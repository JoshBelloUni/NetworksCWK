import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // thread for reading server responses
        Thread responseThread = new Thread(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    System.out.print(">>> ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        responseThread.start();

        String userInputLine;
        while (true) {
            System.out.print(">>> ");

            userInputLine = userInput.readLine();
            if (userInputLine != null) {
                out.println(userInputLine);

                // Check if the user wants to exit
                if (userInputLine.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        }

        userInput.close();
        out.close();
        socket.close();

        // Wait for the response thread to finish
        try {
            responseThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
