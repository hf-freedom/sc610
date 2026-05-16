package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Vehicle;
import com.logistics.dispatch.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public Vehicle createVehicle(Vehicle vehicle) {
        vehicle.setStatus("AVAILABLE");
        vehicle.setCurrentLoadWeight(0.0);
        vehicle.setCurrentLoadVolume(0.0);
        return vehicleRepository.save(vehicle);
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status);
    }

    public List<Vehicle> getAvailableVehicles(String region, Double weight, Double volume) {
        if (weight == null) weight = 0.0;
        if (volume == null) volume = 0.0;
        return vehicleRepository.findAvailableVehicles(region, weight, volume);
    }

    public Vehicle updateVehicleStatus(String id, String status) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            vehicle.setStatus(status);
            return vehicleRepository.save(vehicle);
        }
        return null;
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public boolean deleteVehicle(String id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void initMockData() {
        String[] regions = {"华东区", "华北区", "华南区", "西南区", "华中区"};
        String[] models = {"4.2米厢式货车", "6.8米厢式货车", "9.6米厢式货车", "13米半挂车"};
        String[] plates = {"京A", "沪B", "粤C", "苏D", "浙E", "川F", "鄂G", "鲁H"};

        for (int i = 0; i < 10; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setPlateNumber(plates[i % plates.length] + String.format("%03d", i + 100));
            vehicle.setModel(models[i % models.length]);
            vehicle.setRegion(regions[i % regions.length]);
            
            if (i % 4 == 0) {
                vehicle.setMaxWeight(2000.0);
                vehicle.setMaxVolume(15.0);
            } else if (i % 4 == 1) {
                vehicle.setMaxWeight(5000.0);
                vehicle.setMaxVolume(35.0);
            } else if (i % 4 == 2) {
                vehicle.setMaxWeight(10000.0);
                vehicle.setMaxVolume(55.0);
            } else {
                vehicle.setMaxWeight(30000.0);
                vehicle.setMaxVolume(80.0);
            }
            
            vehicle.setStatus("AVAILABLE");
            vehicleRepository.save(vehicle);
        }
    }
}
