package org.opentripplanner.api.resource;

import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.error.PlannerError;

import javax.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** Represents a trip planner response, will be serialized into XML or JSON by Jersey */
@Getter
@Setter
public class Response implements Serializable {

    // NOTE: the order below is semi-important, in that Jersey will use the
    // same order for the elements in the JS or XML serialized response. The traditional order
    // is request params, followed by plan, followed by errors.

    /** A dictionary of the parameters provided in the request that triggered this response. */
    private HashMap<String, String> requestParameters;
    /** The actual trip plan. */
    private TripPlan plan;
    /** The error (if any) that this response raised. */
    private PlannerError error = null;

    /** Debugging and profiling information */
    public DebugOutput debugOutput = null;

    public ElevationMetadata elevationMetadata = null;

    /** This no-arg constructor exists to make JAX-RS happy. */ 
    @SuppressWarnings("unused")
    private Response() {};

    /** Construct an new response initialized with all the incoming query parameters. */
    public Response(UriInfo info) {
        this.requestParameters = new HashMap<>();
        if (info == null) { 
            // in tests where there is no HTTP request, just leave the map empty
            return;
        }
        for (Entry<String, List<String>> e : info.getQueryParameters().entrySet()) {
            // include only the first instance of each query parameter
            requestParameters.put(e.getKey(), e.getValue().get(0));
        }
    }
}
