package org.opentripplanner.updater.bike_rental;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.util.NonLocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Load bike rental stations from a KML placemarks. Use name as bike park name and point
 * coordinates. Rely on: 1) bike park to be KML Placemarks, 2) geometry to be Point.
 */
public class GenericKmlBikeRentalDataSource extends GenericXmlBikeRentalDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(GenericKmlBikeRentalDataSource.class);

    private String namePrefix = null;

    private Set<String> networks = null;

    private boolean allowDropoff = true;

    /**
     * @param namePrefix A string to prefix all station names coming from this source (for example:
     *        "OV-fietspunt "). Please add a space at the end if needed.
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * @param networks A network, or a comma-separated list of networks, to set to all stations from
     *        the dataSource. Default to null (compatible with all).
     */
    public void setNetworks(String networks) {
        this.networks = new HashSet<String>();
        this.networks.addAll(Arrays.asList(networks.split(",")));
    }

    /**
     * @param allowDropoff True if the bike rental stations coming from this source allows bike
     *        dropoff. True by default.
     */
    public void setAllowDropoff(boolean allowDropoff) {
        this.allowDropoff = allowDropoff;
    }

    public GenericKmlBikeRentalDataSource() {
        super("//*[local-name()='kml']/*[local-name()='Document']/*[local-name()='Placemark']");
    }

    public BikeRentalStation makeStation(Map<String, String> attributes) {
        if (!attributes.containsKey("name")) {
            LOG.warn("Missing name in KML Placemark, cannot create bike rental.");
            return null;
        }
        if (!attributes.containsKey("Point")) {
            LOG.warn("Missing Point geometry in KML Placemark, cannot create bike rental.");
            return null;
        }
        BikeRentalStation brStation = new BikeRentalStation();
        brStation.name = new NonLocalizedString(attributes.get("name").trim());
        if (namePrefix != null)
            brStation.name = new NonLocalizedString(namePrefix + brStation.name);
        String[] coords = attributes.get("Point").trim().split(",");
        brStation.longitude = Double.parseDouble(coords[0]);
        brStation.latitude = Double.parseDouble(coords[1]);
        // There is no ID in KML, assume unique names and location
        brStation.id = String.format(Locale.US, "%s[%.3f-%.3f]", brStation.name.toString().replace(" ", "_"),
                brStation.longitude, brStation.latitude);
        brStation.realTimeData = false;
        brStation.bikesAvailable = 1; // Unknown, always 1
        brStation.spacesAvailable = 1; // Unknown, always 1
        brStation.networks = networks;
        brStation.allowDropoff = allowDropoff;
        return brStation;
    }

    @Override
    public void configure(Graph graph, JsonNode config) {
        super.configure(graph, config);
        setNamePrefix(config.path("namePrefix").asText());
    }

}
