package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SharedVehiclesSnapshotLabel {

    private static final LocalDateTime EMPTY_TIMESTAMP = LocalDateTime.of(0, 1, 1, 0, 0);

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime timestamp;

    public SharedVehiclesSnapshotLabel() {
        this(EMPTY_TIMESTAMP);
    }

    public SharedVehiclesSnapshotLabel(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
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
