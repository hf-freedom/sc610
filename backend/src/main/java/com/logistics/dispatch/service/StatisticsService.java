package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Driver;
import com.logistics.dispatch.model.Statistics;
import com.logistics.dispatch.repository.BatchRepository;
import com.logistics.dispatch.repository.DriverRepository;
import com.logistics.dispatch.repository.OrderRepository;
import com.logistics.dispatch.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private BatchRepository batchRepository;

    public Statistics getOverview() {
        Statistics stats = new Statistics();
        
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus("PENDING"));
        stats.setTransportingOrders(orderRepository.countByStatus("TRANSPORTING") + orderRepository.countByStatus("IN_TRANSIT"));
        stats.setCompletedOrders(orderRepository.countByStatus("COMPLETED"));
        stats.setExceptionOrders(orderRepository.countByStatus("EXCEPTION"));
        
        stats.setTotalVehicles(vehicleRepository.count());
        stats.setAvailableVehicles(vehicleRepository.countByStatus("AVAILABLE"));
        stats.setBusyVehicles(vehicleRepository.countByStatus("BUSY") + vehicleRepository.countByStatus("TRANSPORTING"));
        
        stats.setTotalDrivers(driverRepository.count());
        stats.setAvailableDrivers(driverRepository.countByStatus("AVAILABLE"));
        
        stats.setVehicleUtilizationRate(calculateVehicleUtilization());
        stats.setDriverOnTimeRate(calculateDriverOnTimeRate());
        stats.setRouteDelayRate(calculateRouteDelayRate());
        
        return stats;
    }

    private double calculateVehicleUtilization() {
        long total = vehicleRepository.count();
        if (total == 0) return 0.0;
        long busy = vehicleRepository.countByStatus("BUSY") + vehicleRepository.countByStatus("TRANSPORTING");
        return Math.round((double) busy / total * 10000) / 100.0;
    }

    private double calculateDriverOnTimeRate() {
        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) return 100.0;
        
        double totalRate = drivers.stream()
                .mapToDouble(d -> d.getOnTimeRate() != null ? d.getOnTimeRate() : 100.0)
                .sum();
        return Math.round(totalRate / drivers.size() * 100) / 100.0;
    }

    private double calculateRouteDelayRate() {
        long totalBatches = batchRepository.count();
        if (totalBatches == 0) return 0.0;
        
        long delayedCount = orderRepository.countByStatus("EXCEPTION");
        return Math.round((double) delayedCount / totalBatches * 10000) / 100.0;
    }

    public Map<String, Long> getOrderStatusDistribution() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN",
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getRegionDistribution() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getRegion() != null ? o.getRegion() : "未知区域",
                        Collectors.counting()
                ));
    }
}
