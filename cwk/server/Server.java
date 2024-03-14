import java.io.*;
import java.net.*;
import java.io.File;

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
            out.println("Folder exists");

            File[] files = folder.listFiles();
            
            // iterate thru files and print the name to the client
            if (files != null && files.length > 0) {
                out.println("Files available:");
                for (File file : files) {
                    if (file.isFile()) {
                        out.println(file.getName());
                    }
                }
            } else {
                out.println("No files available");
            }
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
                }
                else {
                    out.println("Server echoed: " + inputLine);
                }
            }

            in.close();
            out.close();
            clientSocket.close();

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
