import java.io.*;
import java.net.*;
import java.io.File;

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /* 
    private void listFiles(PrintWriter out) {
        out.println("printing...");
        System.out.println("Inside listFiles() method.");
        File folder = new File("server/serverFiles");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    out.println(file.getName());;
                }
            }
        }
    }
    */
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);               

                if (inputLine.equals("list")) {
                    System.out.println("Printing Files"); 
                    out.println("printing...");
                    File folder = new File("server/serverFiles");
                    File[] files = folder.listFiles();

                    out.println(files);
                }
                else {
                    out.println("Server echoed: " + inputLine);
                }
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
