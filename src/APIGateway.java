public class APIGateway {
    private final CommunicationServerStrategy strategy;

    public APIGateway(CommunicationServerStrategy strategy) {
        this.strategy = strategy;
    }

    public void start(BalanceUpdateProcessor processor, int port) {
        strategy.start(processor, port);
    }

    public void stop() {
        strategy.stop();
    }
}
