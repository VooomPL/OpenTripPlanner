package org.opentripplanner.routing.graph.Estimators.GraphComponent;

import java.io.Serializable;
import java.util.Objects;

public class GraphComponent implements Serializable {
    private int componentId;
    private int size;

    public GraphComponent(int componentId, int size) {
        this.componentId = componentId;
        this.size = size;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getSize() {
        return size;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphComponent that = (GraphComponent) o;
        return componentId == that.componentId &&
                size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, size);
    }
}
