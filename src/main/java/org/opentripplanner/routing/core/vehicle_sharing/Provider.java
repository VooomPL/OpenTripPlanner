package org.opentripplanner.routing.core.vehicle_sharing;

import java.util.Objects;

public class Provider {

    private final int id;
    private final String name;

    public Provider(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provider provider = (Provider) o;
        return id == provider.id &&
                Objects.equals(name, provider.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
