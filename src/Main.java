import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        BalanceUpdateProcessor processor = new BalanceUpdateProcessor();

        // Aqui escolhemos a estratégia de rede
//        CommunicationServerStrategy strategy = new UdpServerStrategy();
//        CommunicationServerStrategy strategy = new TcpServerStrategy();
        CommunicationServerStrategy strategy = new HttpServerStrategy();
        APIGateway gateway = new APIGateway(strategy);

        // Inicia o servidor
        gateway.start(processor, 9003);

        // Adicione lógica de desligamento se necessário, ex: shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gateway.stop();
            processor.shutdown();
        }));
    }
}