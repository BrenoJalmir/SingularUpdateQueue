import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;

public class UdpServerStrategy implements CommunicationServerStrategy {
    private DatagramSocket socket;
    private boolean running = false;

    @Override
    public void start(BalanceUpdateProcessor processor, int port) {
        try {
            socket = new DatagramSocket(port);
            running = true;
            System.out.println("UDP Server started on port " + port);

            new Thread(() -> {
                while (running) {
                    try {
                        byte[] buffer = new byte[1024];
                        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(requestPacket);

                        String request = new String(requestPacket.getData(), 0, requestPacket.getLength()).trim();

                        CompletableFuture<String> responseFuture = processor.submitUpdate(request);
                        responseFuture.thenAccept(response -> sendResponse(response, requestPacket));
                    } catch (IOException e) {
                        if (running) e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao iniciar servidor UDP", e);
        }
    }

    private void sendResponse(String response, DatagramPacket requestPacket) {
        try {
            byte[] data = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                    data, data.length,
                    requestPacket.getAddress(),
                    requestPacket.getPort()
            );
            socket.send(responsePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
