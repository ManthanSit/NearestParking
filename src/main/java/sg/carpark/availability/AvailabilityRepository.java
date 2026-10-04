package sg.carpark.availability;

public interface AvailabilityRepository {
    void publish(AvailabilitySnapshot snapshot);

    boolean hasSnapshot();

    AvailabilitySnapshot getSnapshot();
}
