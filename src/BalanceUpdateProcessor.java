import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

public class BalanceUpdateProcessor {
    private final SingularUpdateQueue<String, String> updateQueue;
    private final Bank bank;

    public BalanceUpdateProcessor() {
        this.updateQueue = new SingularUpdateQueue<>();
        this.bank = new Bank();
        this.updateQueue.setHandler(this::doAction);
        this.updateQueue.start();
    }

    private String doAction(String message) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(message, "|");
            String action = tokenizer.nextToken();
            String accountId = tokenizer.nextToken();
            float value;
            try {
                value = Float.parseFloat(tokenizer.nextToken());
            } catch (NumberFormatException e) {
                value = 0;
            }
            if (action.equalsIgnoreCase("criar")) {
                System.out.println("Criando conta de número " + accountId + ".");
                this.bank.addAccount(accountId);
            } else if (action.equalsIgnoreCase("depositar")) {
                System.out.println("Depositando R$ " + String.format("%.2f", value) + " na conta de número " + accountId + ".");
                this.bank.makeDeposit(accountId, value);
            } else if (action.equalsIgnoreCase("saldo"))
                System.out.println("Saldo de R$ " + String.format("%.2f", this.bank.getBalance(accountId)) + " na conta de número " + accountId + ".");
            else {
                System.out.println("Comando inválido.");
            }
            return "Tudo OK.";
        } catch (Exception e) {
            return "Deu errado.";
        }
    }

    public CompletableFuture<String> submitUpdate(String message) {
        return updateQueue.submit(message);
    }

    public void shutdown() {
        updateQueue.shutdown();
    }
}
