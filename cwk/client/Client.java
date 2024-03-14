import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // thread for reading server responses
        Thread responseThread = new Thread(() -> {
            try {
                // look for response
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    in.close();
                    out.close();
                    socket.close();
                }
                // catch error
            } catch (IOException e) {
                e.printStackTrace();
                // if there is a response, close    
            }
        });
        responseThread.start();

        if (args.length == 0) {
            System.out.println("Usage: Client [command] [fname]");
            out.close();
            socket.close();
        } else {
            out.println(args[0]);
        }
        
        // close on error
        try {
            responseThread.join();
            out.close();
            socket.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
