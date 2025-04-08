public interface CommunicationServerStrategy {
    void start(BalanceUpdateProcessor processor, int port);
    void stop();
}
