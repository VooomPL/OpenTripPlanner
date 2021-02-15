package org.opentripplanner.updater.transit.ticket.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.updater.transit.ticket.parser.ConstraintsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransitTicketDeserializer extends StdDeserializer<TransitTicket> {

    private static final Logger LOG = LoggerFactory.getLogger(TransitTicketDeserializer.class);

    public TransitTicketDeserializer() {
        this(null);
    }

    public TransitTicketDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TransitTicket deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode mainNode = codec.readTree(jsonParser);
        JsonNode idNode = mainNode.get("id");
        JsonNode standardPriceNode = mainNode.get("standard_price");
        TransitTicket.TransitTicketBuilder transitTicketBuilder = TransitTicket.builder(
                Objects.nonNull(idNode) ? idNode.asInt(-1) : -1,
                Objects.nonNull(standardPriceNode) ? BigDecimal.valueOf(standardPriceNode.asDouble(-1)) : BigDecimal.valueOf(-1)
        );

        JsonNode maxTimeNode = mainNode.get("max_time");
        if (Objects.nonNull(maxTimeNode)) {
            transitTicketBuilder.setTimeLimit(maxTimeNode.asInt(-1));
        }

        JsonNode maxDistanceNode = mainNode.get("max_distance");
        if (Objects.nonNull(maxDistanceNode)) {
            transitTicketBuilder.setDistanceLimit(maxDistanceNode.asInt(-1));
        }

        JsonNode maxFaresNode = mainNode.get("max_fares");
        if (Objects.nonNull(maxFaresNode)) {
            transitTicketBuilder.setFaresNumberLimit(maxFaresNode.asInt(-1));
        }

        JsonNode availableFromNode = mainNode.get("available_from");
        if (Objects.nonNull(availableFromNode) && Objects.nonNull(availableFromNode.asText())) {
            try {
                LocalDateTime availableFrom = LocalDateTime.parse(availableFromNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                transitTicketBuilder.setAvailableFrom(availableFrom);
            } catch (DateTimeParseException e) {
                LOG.warn("Unrecognized available from date format '{}' in ticket definition", availableFromNode.asText());
            }
        }

        JsonNode availableToNode = mainNode.get("available_to");
        if (Objects.nonNull(availableToNode) && Objects.nonNull(availableToNode.asText())) {
            try {
                LocalDateTime availableTo = LocalDateTime.parse(availableToNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                transitTicketBuilder.setAvailableTo(availableTo);
            } catch (DateTimeParseException e) {
                LOG.warn("Unrecognized available to date format '{}' in ticket definition", availableToNode.asText());
            }
        }

        TransitTicket returnedTicket = transitTicketBuilder.build();

        JsonNode allowedAgenciesNode = mainNode.get("allowed_agencies");
        if (Objects.nonNull(allowedAgenciesNode)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> rulesForAgency = mapper.convertValue(allowedAgenciesNode,
                    new TypeReference<HashMap<String, List<String>>>() {
                    });
            for (String agencyId : rulesForAgency.keySet()) {
                ConstraintsParser.parseConstraints(returnedTicket, agencyId, rulesForAgency.get(agencyId));
            }
        }

        JsonNode fareSwitchRulesNode = mainNode.get("fare_switch_rules");
        if (Objects.nonNull(fareSwitchRulesNode)) {
            ObjectMapper fareSwitchMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule("FareSwitchPatternDeserializer",
                    new Version(1, 0, 0, null, null, null));
            module.addDeserializer(FareSwitchPattern.class, new FareSwitchPatternDeserializer());
            fareSwitchMapper.registerModule(module);

            List<FareSwitchPattern> fareSwitchPatterns = fareSwitchMapper.convertValue(fareSwitchRulesNode,
                    new TypeReference<List<FareSwitchPattern>>() {
                    });
            for (FareSwitchPattern fareSwitchPattern : fareSwitchPatterns) {
                returnedTicket.getFareSwitchPatterns().add(fareSwitchPattern);
            }
        }

        return returnedTicket;
    }

}
