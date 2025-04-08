import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

public class HttpServerStrategy implements CommunicationServerStrategy {
    private boolean running = false;

    @Override
    public void start(BalanceUpdateProcessor processor, int port) {
        running = true;
        System.out.println("HTTP Server started on port " + port);

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port, 300)) {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket, processor)).start();
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket clientSocket, BalanceUpdateProcessor processor) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String requestLine = in.readLine();

            if (requestLine == null || requestLine.isEmpty()) {
                sendResponse(out, 400, "Bad Request");
                return;
            }

            StringTokenizer tokenizer = new StringTokenizer(requestLine);
            String method = tokenizer.nextToken();
            String path = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "/";

            if (!method.equalsIgnoreCase("POST")) {
                sendResponse(out, 405, "Method Not Allowed");
                return;
            }

            // Lê headers até encontrar uma linha em branco
            int contentLength = 0;
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            char[] bodyChars = new char[contentLength];
            int bytesRead = in.read(bodyChars);
            if (bytesRead != contentLength) {
                sendResponse(out, 400, "Corpo incompleto");
                return;
            }

            String requestBody = new String(bodyChars).trim();
            System.out.println("HTTP Request: " + requestBody);

            CompletableFuture<String> resultFuture = processor.submitUpdate(requestBody);

            resultFuture.thenAccept(responseBody -> {
                try {
                    sendResponse(out, 200, responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).join(); // Espera o envio terminar antes de encerrar o socket
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void sendResponse(DataOutputStream out, int statusCode, String body) throws IOException {
        String statusLine;
        switch (statusCode) {
            case 200 -> statusLine = "HTTP/1.1 200 OK\r\n";
            case 400 -> statusLine = "HTTP/1.1 400 Bad Request\r\n";
            case 405 -> statusLine = "HTTP/1.1 405 Method Not Allowed\r\n";
            default -> statusLine = "HTTP/1.1 500 Internal Server Error\r\n";
        }

        String headers =
                "Content-Type: text/plain\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "\r\n";

        out.writeBytes(statusLine);
        out.writeBytes(headers);
        out.writeBytes(body);
        out.flush();
    }

    @Override
    public void stop() {
        running = false;
        System.out.println("HTTP Server stopped.");
    }
}
