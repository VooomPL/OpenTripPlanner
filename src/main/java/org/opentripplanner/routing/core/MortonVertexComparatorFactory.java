package org.opentripplanner.routing.core;

import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.graph.VertexComparatorFactory;

import java.io.Serializable;
import java.util.List;

public class MortonVertexComparatorFactory implements VertexComparatorFactory, Serializable {
    private static final long serialVersionUID = -6904862616793682390L;

    public MortonVertexComparator getComparator(List<Vertex> domain) {
        return new MortonVertexComparator(domain);
    }

}