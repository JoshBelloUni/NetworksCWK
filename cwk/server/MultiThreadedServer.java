import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServer {
    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(20);

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Listening on port 12345...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client Connected:");

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
}
