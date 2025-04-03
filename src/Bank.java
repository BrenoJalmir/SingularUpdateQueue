import java.util.HashMap;

public class Bank {
    private final HashMap<String, Float> accounts = new HashMap<>();
    public void addAccount(String accountId) { this.accounts.put(accountId, 0.0f); }
    public void makeDeposit(String accountId, float value) {
        if (this.accounts.get(accountId) == null) addAccount(accountId);
        this.accounts.put(accountId, this.accounts.get(accountId) + value);
    }
    public float getBalance(String accountId) {
        Float value = this.accounts.get(accountId);
        if (value == null) addAccount(accountId);
        return this.accounts.get(accountId);
    }
}
