import java.io.*;
import java.net.*;

public class Client {

    static boolean checkValidFile(String fname) {

        File file = new File(fname);
        if (!file.exists()) {
            System.out.println("File does not exist: " + fname);
            return false;
        } 
        return true;
        
    }

    static void sendFile(String fname, Socket socket) {

        try {
            System.out.println("Sending file data to input stream");

            FileInputStream fileInput = new FileInputStream(fname);
            OutputStream fileOutput = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;

            // read one byte in the file
            // check if its a valid byte
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > 64*1024) {
                    System.out.println("Maximum file size limit (64KB) exceeded.");
                    break;
                }
                fileOutput.write(buffer,0,bytesRead);
            }

            fileInput.close();
            System.out.println("Successfully Sent to server");

        } catch (IOException e) {
            System.out.println("Error occurred while sending file: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        // input and output buffers
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // thread for reading server responses
        Thread responseThread = new Thread(() -> {
            try {
                // look for response
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Server Response: " + response);
                    if (!in.ready()) {
                        break;
                    }
                }
                // catch error
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        responseThread.start();


        if (args.length == 0) {
            System.out.println("Usage: Client [command] [fname]");
            out.println("invalid"); // send invalid to the server 
        } else {
            if (args[0].equals("list")) {
                out.println(args[0]);   // send the word list to the server
            } else if (args[0].equals("put")) {
                if (args.length != 2) {
                    System.out.println("Usage: Client put [fname]");
                    out.println("invalid");
                } else {
                    if (checkValidFile(args[1]) == true) {
                        out.println(args[0]);   // send command
                        out.println(args[1]);   // send filename
                        sendFile(args[1], socket);
                    } else {
                        out.println("invalid");
                    }
                }
            } else {
                System.out.println("Invalid Command: " + args[0]);
                out.println("invalid");
            }
        }

        try {
            System.out.println("Waiting for response");
            responseThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Close resources and disconnect
            try {
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Disconnecting...");
        }
    }
}
