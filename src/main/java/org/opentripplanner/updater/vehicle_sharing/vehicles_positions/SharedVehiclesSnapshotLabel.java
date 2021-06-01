package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SharedVehiclesSnapshotLabel {

    private static final LocalDateTime EMPTY_TIMESTAMP = LocalDateTime.of(0, 1, 1, 0, 0);

    LocalDateTime timestamp;

    public SharedVehiclesSnapshotLabel() {
        this(EMPTY_TIMESTAMP);
    }

    public SharedVehiclesSnapshotLabel(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isEmpty() {
        return timestamp.equals(EMPTY_TIMESTAMP);
    }

    public String toString() {
        if (this.isEmpty()) {
            return "'current'";
        } else {
            return timestamp.toString();
        }
    }

}
