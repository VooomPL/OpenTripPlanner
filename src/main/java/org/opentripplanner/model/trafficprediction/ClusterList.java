package org.opentripplanner.model.trafficprediction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClusterList {

    private List<Cluster> clusters;

    public ClusterList(String jsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream fileStream = new FileInputStream(jsonPath)) {
            this.clusters = mapper.readValue(fileStream, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Cluster.class));
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> Clusters) {
        this.clusters = Clusters;
    }
}
