package sg.carpark.catalog;

public class CatalogRefreshFailedException extends IllegalStateException {
    public CatalogRefreshFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
