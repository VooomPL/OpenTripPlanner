package org.opentripplanner.graph_builder.module.transit.tickets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Value;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Value
public class AvailableTransitTicketsReader {

    private static final Logger LOG = LoggerFactory.getLogger(AvailableTransitTicketsReader.class);

    ObjectMapper objectMapper;

    public AvailableTransitTicketsReader() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Set<TransitTicket> getFromFile(File sourceFile) {
        Set<TransitTicket> transitTickets = null;

        try {
            String json = new String(Files.readAllBytes(sourceFile.toPath()));
            transitTickets = objectMapper.readValue(json, new TypeReference<HashSet<TransitTicket>>() {
            });
        } catch (JsonProcessingException e) {
            LOG.error("File {} containing transit tickets definitions malformed", sourceFile.getName());
        } catch (NoSuchFileException e) {
            LOG.warn("File {} containing transit tickets definitions not found", sourceFile.getName());
        } catch (IOException e) {
            LOG.error("Problems occurred when trying to read ticket definitions {} file", sourceFile.getName());
        } catch (NullPointerException e) {
            LOG.warn("Tickets definitions file name is null");
        }
        if (Objects.isNull(transitTickets)) return new HashSet<>();

        return transitTickets;
    }

}
