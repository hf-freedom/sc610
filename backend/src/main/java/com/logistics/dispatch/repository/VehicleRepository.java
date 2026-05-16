package com.logistics.dispatch.repository;

import com.logistics.dispatch.model.Vehicle;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class VehicleRepository {
    private final Map<String, Vehicle> vehicleMap = new ConcurrentHashMap<>();
    private int vehicleCounter = 1;

    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            vehicle.setId(UUID.randomUUID().toString());
            vehicle.setCreateTime(new Date());
        }
        vehicleMap.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(vehicleMap.get(id));
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicleMap.values());
    }

    public List<Vehicle> findByStatus(String status) {
        return vehicleMap.values().stream()
                .filter(vehicle -> status.equals(vehicle.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Vehicle> findByRegion(String region) {
        return vehicleMap.values().stream()
                .filter(vehicle -> region.equals(vehicle.getRegion()))
                .collect(Collectors.toList());
    }

    public List<Vehicle> findAvailableVehicles(String region, Double weight, Double volume) {
        return vehicleMap.values().stream()
                .filter(vehicle -> "AVAILABLE".equals(vehicle.getStatus()))
                .filter(vehicle -> region == null || region.isEmpty() || region.equals(vehicle.getRegion()))
                .filter(vehicle -> weight == null || weight <= 0 || vehicle.getMaxWeight() >= weight)
                .filter(vehicle -> volume == null || volume <= 0 || vehicle.getMaxVolume() >= volume)
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        vehicleMap.remove(id);
    }

    public boolean existsById(String id) {
        return vehicleMap.containsKey(id);
    }

    public long count() {
        return vehicleMap.size();
    }

    public long countByStatus(String status) {
        return vehicleMap.values().stream()
                .filter(vehicle -> status.equals(vehicle.getStatus()))
                .count();
    }
}
