import java.util.concurrent.CompletableFuture;

public class RequestWrapper<Req, Res> {
    private final CompletableFuture<Res> future;
    private final Req request;

    public RequestWrapper(Req request) {
        this.request = request;
        this.future = new CompletableFuture<Res>();
    }

    public CompletableFuture<Res> getFuture() { return future; }
    public Req getRequest() { return request; }

    public void complete(Res response) {
        future.complete(response);
    }

    public void completeExceptionally(Exception e) {
        future.completeExceptionally(e);
    }
}
