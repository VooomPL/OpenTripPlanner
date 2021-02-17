package org.opentripplanner.updater.transit.ticket.parser;

import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConstraintsParser {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintsParser.class);

    public static void parseConstraints(TransitTicket ticket, String agencyId, List<String> constraintsAsText) {
        ticket.addAllowedAgency(agencyId);
        parseConstraints(ticket.getRoutePattern(agencyId), ticket.getStopPattern(agencyId), constraintsAsText);
    }

    public static void parseConstraints(RoutePattern routePattern, StopPattern stopPattern, List<String> constraintsAsText) {
        String[] constraintElements;
        String[] objectDefinitionElements;
        String objectAsText, objectPropertyAsText, operatorAsText, patternAsText;

        for (String constraintAsText : constraintsAsText) {
            constraintElements = constraintAsText.split(" ");
            if (constraintElements.length >= 3) {
                objectDefinitionElements = constraintElements[0].split("\\.");

                if (objectDefinitionElements.length == 2) {

                    objectAsText = objectDefinitionElements[0];
                    objectPropertyAsText = objectDefinitionElements[1];
                    operatorAsText = constraintElements[1];
                    patternAsText = constraintAsText.substring(constraintAsText.indexOf(" ",
                            objectAsText.length() + objectPropertyAsText.length() + operatorAsText.length() + 1)).trim();

                    if (objectAsText.equals(Route.class.getSimpleName())) {
                        RouteConstraintsParser.parseConstraints(routePattern, objectPropertyAsText, operatorAsText, patternAsText);
                    } else if (objectAsText.equals(Stop.class.getSimpleName())) {
                        StopConstraintsParser.parseConstraints(stopPattern, objectPropertyAsText, operatorAsText, patternAsText);
                    } else {
                        LOG.warn("Unrecognized rule object name '{}' in ticket definition", objectAsText);
                    }
                } else {
                    LOG.warn("Invalid rule object structure for constraint '{}' in ticket definition", constraintAsText);
                }
            } else {
                LOG.warn("Invalid rule structure for constraint '{}' in ticket definition", constraintAsText);
            }
        }
    }

}
