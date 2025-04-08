import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class TcpServerStrategy implements CommunicationServerStrategy {
    private ServerSocket serverSocket;
    private boolean running = false;

    @Override
    public void start(BalanceUpdateProcessor processor, int port) {
        try {
            serverSocket = new ServerSocket(port, 300);
            running = true;
            System.out.println("TCP Server started on port " + port);

            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();

                        new Thread(() -> handleClient(clientSocket, processor)).start();
                    } catch (IOException e) {
                        if (running) e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao iniciar servidor TCP", e);
        }
    }

    private void handleClient(Socket clientSocket, BalanceUpdateProcessor processor) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message = in.readLine();
            if (message == null) {
                out.println("Empty message received.");
                return;
            }

            CompletableFuture<String> response = processor.submitUpdate(message);
            response.thenAccept(serverResponse -> {
                out.println(serverResponse);
                out.flush();
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }
}
