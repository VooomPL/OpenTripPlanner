package org.opentripplanner.graph_builder.module.transit.tickets.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.opentripplanner.graph_builder.module.transit.tickets.parser.ConstraintsParser;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FareSwitchPatternDeserializer extends StdDeserializer<FareSwitchPattern> {

    private static final Logger LOG = LoggerFactory.getLogger(FareSwitchPatternDeserializer.class);

    public FareSwitchPatternDeserializer() {
        this(null);
    }

    public FareSwitchPatternDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FareSwitchPattern deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode mainNode = codec.readTree(jsonParser);
        boolean reverseAllowed = false;

        JsonNode reverseAllowedNode = mainNode.get("reverse_allowed");
        if (Objects.nonNull(reverseAllowedNode)) {
            reverseAllowed = reverseAllowedNode.asBoolean(false);
        }

        FareSwitchPattern returnedPattern = new FareSwitchPattern(new RoutePattern(), new RoutePattern(),
                new StopPattern(), new StopPattern(), reverseAllowed);

        JsonNode previousFarePatternNode = mainNode.get("previous_fare");
        if (Objects.nonNull(previousFarePatternNode)) {
            ObjectMapper mapper = new ObjectMapper();
            List<String> previousFareConstraints = mapper.convertValue(previousFarePatternNode,
                    new TypeReference<List<String>>() {
                    });
            ConstraintsParser.parseConstraints(returnedPattern.getPreviousRoutePattern(), returnedPattern.getPreviousStopPattern(),
                    previousFareConstraints);
        }

        JsonNode futureFarePatternNode = mainNode.get("future_fare");
        if (Objects.nonNull(futureFarePatternNode)) {
            ObjectMapper mapper = new ObjectMapper();
            List<String> futureFareConstraints = mapper.convertValue(futureFarePatternNode,
                    new TypeReference<List<String>>() {
                    });
            ConstraintsParser.parseConstraints(returnedPattern.getFutureRoutePattern(), returnedPattern.getFutureStopPattern(),
                    futureFareConstraints);
        }

        return returnedPattern;
    }

}
