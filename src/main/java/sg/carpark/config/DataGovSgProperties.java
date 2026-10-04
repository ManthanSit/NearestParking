package sg.carpark.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "data-gov-sg")
public class DataGovSgProperties {
    private String catalogueUrl;
    private String catalogueResourceId;
    private String availabilityUrl;

    public String getCatalogueUrl() {
        return catalogueUrl;
    }

    public void setCatalogueUrl(String catalogueUrl) {
        this.catalogueUrl = catalogueUrl;
    }

    public String getCatalogueResourceId() {
        return catalogueResourceId;
    }

    public void setCatalogueResourceId(String catalogueResourceId) {
        this.catalogueResourceId = catalogueResourceId;
    }

    public String getAvailabilityUrl() {
        return availabilityUrl;
    }

    public void setAvailabilityUrl(String availabilityUrl) {
        this.availabilityUrl = availabilityUrl;
    }
}
