package sg.carpark.catalog;

public class CatalogRefreshCooldownException extends RuntimeException {
    private final long retryAfterSeconds;

    public CatalogRefreshCooldownException(long retryAfterSeconds) {
        super("Catalogue refresh is cooling down");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
