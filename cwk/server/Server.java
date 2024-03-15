import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static int connectedClients = 0;
    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(20);

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Listening on port 12345...");

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
        } finally {
            if (service != null) {
                service.shutdown();
            }
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
        out.println("Printing Files...");           // print to client

        File folder = new File("./serverFiles");

        // check folder exists
        if (!folder.exists() || !folder.isDirectory()) {
            out.println("Folder not found");
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
            InputStream fileInput = socket.getInputStream();
            FileOutputStream fileOutput = new FileOutputStream("./serverFiles/" + fname);
            byte[] buffer = new byte[1024];
            int bytesRead;

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

    public void run() {
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);               
                if (inputLine.equals("list")) {
                    listFiles(out);
                } else if (inputLine.equals("put")) {
                    String fileName = in.readLine();
                    if (fileName != null) {
                        putFile(out, fileName, clientSocket);
                    } else {
                        System.out.println("Error: No file specified");
                    }
                }
            }

            in.close();
            out.close();
            System.out.println("Client Disconnected");

        } catch (IOException e) {
            System.out.println("Client Disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                Server.decrementConnectedClients();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
