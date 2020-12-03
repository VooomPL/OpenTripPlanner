package org.opentripplanner.estimator.utils;

import org.opentripplanner.estimator.hasura_client.ProvidersGetter;
import org.opentripplanner.estimator.hasura_client.VehicleStateSnapshotGetter;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.graph.Graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseSnapshotDownloader {

    private Graph graph;
    private String databaseURL;
    private String databasePassword;
    private Map<Integer, Provider> vehicleProviders;

    public DatabaseSnapshotDownloader(Graph graph, String databaseURL, String databasePassword) {
        this.graph = graph;
        this.databaseURL = databaseURL;
        this.vehicleProviders = new HashMap<>();
        this.databasePassword = databasePassword;
    }

    public void initializeProviders() {
        ProvidersGetter providersGetter = new ProvidersGetter();
        this.vehicleProviders = providersGetter.getFromHasura(this.graph, this.databaseURL).stream()
                .collect(Collectors.toMap(Provider::getProviderId, provider -> provider));
    }

    public int downloadSnapshot(LocalDateTime timestamp) {
        VehicleStateSnapshotGetter vehicleSnapshotGetter = new VehicleStateSnapshotGetter(this.vehicleProviders, timestamp);
        List<VehicleDescription> vehicles = vehicleSnapshotGetter.getFromHasuraWithPassword(this.graph, this.databaseURL, this.databasePassword);
        if (Objects.nonNull(vehicles)) {
            saveSnapshotData(vehicles);
            return vehicles.size();
        }
        return 0;
        //TODO: why timeouts??? Even when session and socket timeouts are increased - gateway timeouts occur
    }

    private void saveSnapshotData(List<VehicleDescription> vehicles) {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter("current_snapshot.json"))) {
            fileWriter.write("{\"data\":{\"items\":[");
            for (VehicleDescription vehicle : vehicles) {
                fileWriter.write("{\"providerVehicleId\":\"" + vehicle.getProviderVehicleId() +
                        "\",\"latitude\":" + vehicle.getLatitude() +
                        ",\"longitude\":" + vehicle.getLongitude() +
                        ",\"fuelType\":\"" + vehicle.getFuelType() +
                        "\",\"gearbox\":" + vehicle.getGearbox() +
                        ",\"type\":\"" + VehicleType.getDatabaseVehicleType(vehicle.getVehicleType()) +
                        "\",\"range\":" + vehicle.getRangeInMeters() +
                        ",\"provider\":{\"providerId\":" + vehicle.getProvider().getProviderId() +
                        ",\"providerName\":\"" + vehicle.getProvider().getProviderName() + "\"}," +
                        "\"kmPrice\":" + vehicle.getVehiclePricingPackage(0).getKilometerPrice() +
                        ",\"drivingPrice\":" + vehicle.getVehiclePricingPackage(0).getDrivingPricePerTimeTickInPackageExceeded() +
                        ",\"startPrice\":" + vehicle.getVehiclePricingPackage(0).getStartPrice() +
                        ",\"stopPrice\":" + vehicle.getVehiclePricingPackage(0).getParkingPricePerTimeTickInPackageExceeded() +
                        ",\"maxDailyPrice\":" + vehicle.getVehiclePricingPackage(0).getMaxRentingPrice() +
                        "}");
            }
            fileWriter.write("]}}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
