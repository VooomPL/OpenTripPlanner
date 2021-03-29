package org.opentripplanner.estimator.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("data")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Value
public class SnapshotData {

    List<Vehicle> items = new ArrayList<>();

    public void addAll(List<Vehicle> items) {
        this.items.addAll(items);
    }
}
