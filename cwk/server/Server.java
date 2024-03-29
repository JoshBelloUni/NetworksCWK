import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Server {

    private static int connectedClients = 0;
    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(20);

        try (ServerSocket serverSocket = new ServerSocket(9555)) {
            System.out.println("Server started. Listening on port 9555...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client Connected:");
                System.out.println("    IP Address: " + client.getInetAddress().getHostAddress());
                System.out.println("    Host Name: " + client.getInetAddress().getHostName());
                System.out.println("    Port Number: " + client.getPort());
                System.out.println("    Socket Address: " + client.getRemoteSocketAddress());

                connectedClients++;
                System.out.println("    Connected Clients: " + connectedClients);

                ClientHandler clientHandler = new ClientHandler(client);
                service.submit(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void decrementConnectedClients() {
        connectedClients--;
        System.out.println("    Connected Clients: " + connectedClients);
    }
}

// function to handle listing the files
class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    static void listFiles(PrintWriter out) {

        System.out.println("Printing Files...");    // print to server

        File folder = new File("./serverFiles/");
        

        // check folder exists and if its a dir
        if (!folder.exists() || !folder.isDirectory()) {
            out.println("Folder not found");
            System.out.println("Folder not found");
        } else {
            File[] files = folder.listFiles();
            // iterate thru files and print the name to the client
            if (files != null && files.length > 0) {
                out.println("Files available:");
                for (File file : files) {
                    if (file.isFile()) {
                        out.print("> ");
                        out.println(file.getName());
                    }
                }
            } else {
                out.println("No files available");
            }
        }
    }

    // function to handle putting the file onto the server
    static void putFile(PrintWriter out, String fname, Socket socket) {
        try {
            System.out.println("Accepting file: " + fname);

            out.println("File on server input stream: " + fname);

            // set up the input to he the stream from the client
            InputStream fileInput = socket.getInputStream();
            FileOutputStream fileOutput = new FileOutputStream("./serverFiles/" + fname);
            byte[] buffer = new byte[1024];
            int bytesRead;

            // iterate thru the bytes
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }
            out.println("File received successfully.");
            fileInput.close();
            fileOutput.close();
            System.out.println("File received successfully.");

        } catch (IOException e) {
            System.out.println("Unable to put file");
        }
    }

    // function to print to the log file
    static void logRequest(String request, Socket clientSocket) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logfile.txt", true))) {
            writer.write(formattedDateTime + " | ");
            writer.write(clientSocket.getInetAddress().getHostAddress() + " | ");
            writer.write(request + "\n");

        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    static boolean checkFileExists(String fname) {
        File file = new File("./serverFiles/" + fname);
        if (file.exists()) {
            System.out.println("File already exists: " + fname);
            return true;
        }
        System.out.println("File does not exist: " + fname);
        return false;
    }

    public void run() {
        try {

            // initialise the server input and output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // read from the input buffer
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                
                // handling list command
                if (inputLine.equals("list")) {
                    listFiles(out);
                    logRequest("list", clientSocket);

                // handling put command
                } else if (inputLine.equals("put")) {
                    String fileName = in.readLine();    // read extra line for the filename input
                    if (fileName != null) {
                        if (checkFileExists(fileName) == false) {
                            putFile(out, fileName, clientSocket);
                            logRequest("put " + fileName, clientSocket);
                        } else {
                            out.println("File already exists on server: ");
                            logRequest("put " + fileName, clientSocket);
                            // clear the buffered reader from the contents of the file
                            while ((inputLine = in.readLine()) != null) {
                                in.readLine();
                            }
                        }
                    }
                } else if (inputLine.equals("invalid")) {
                    out.println("Invalid data recieved");
                }
            }



            in.close();
            out.close();

            System.out.println("Client Disconnected");

        } catch (IOException e) {
            System.out.println("Client Disconnected: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Server.decrementConnectedClients();
        }
    }
}
