package org.opentripplanner.updater.transit.ticket;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.updater.transit.ticket.deserializer.TransitTicketDeserializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AvailableTransitTicketsGetter {

    public Set<TransitTicket> getFromFile(String filename) {
        Set<TransitTicket> transitTickets = null;

        try {
            String json = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule("TransitTicketDeserializer",
                    new Version(1, 0, 0, null, null, null));
            module.addDeserializer(TransitTicket.class, new TransitTicketDeserializer());
            objectMapper.registerModule(module);
            transitTickets = objectMapper.readValue(json, new TypeReference<HashSet<TransitTicket>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(transitTickets)) return new HashSet<>();

        return transitTickets;
    }

}
