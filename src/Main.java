import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void replyToClient(String replyMsg, DatagramPacket clientPacket, DatagramSocket serversocket) {
        try {
            byte[] replymsg = replyMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(replymsg, replymsg.length,
                    clientPacket.getAddress(), clientPacket.getPort());
            serversocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BalanceUpdateProcessor processor = new BalanceUpdateProcessor();

        System.out.println("UDP Server Bank started");
        try {
            DatagramSocket serversocket = new DatagramSocket(9003);
            while (true) {
                byte[] receivemessage = new byte[1024];
                DatagramPacket receivepacket = new DatagramPacket(receivemessage, receivemessage.length);
                serversocket.receive(receivepacket);
                String message = new String(receivepacket.getData()).trim();
                CompletableFuture<String> response = processor.submitUpdate(message);

                response.thenAccept(replyMsg -> replyToClient(replyMsg, receivepacket, serversocket));;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UDP Bank server terminating");

        processor.shutdown();
    }
}