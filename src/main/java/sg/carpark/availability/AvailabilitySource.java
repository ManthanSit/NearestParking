package sg.carpark.availability;

public interface AvailabilitySource {
    SourceAvailabilitySnapshot fetchAvailability();
}
