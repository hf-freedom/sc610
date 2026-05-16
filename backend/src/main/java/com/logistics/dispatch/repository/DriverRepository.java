package com.logistics.dispatch.repository;

import com.logistics.dispatch.model.Driver;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DriverRepository {
    private final Map<String, Driver> driverMap = new ConcurrentHashMap<>();
    private int driverCounter = 1;

    public Driver save(Driver driver) {
        if (driver.getId() == null) {
            driver.setId(UUID.randomUUID().toString());
            driver.setCreateTime(new Date());
            driver.setOnTimeRate(100.0);
            driver.setTotalOrders(0);
            driver.setOnTimeOrders(0);
        }
        driverMap.put(driver.getId(), driver);
        return driver;
    }

    public Optional<Driver> findById(String id) {
        return Optional.ofNullable(driverMap.get(id));
    }

    public List<Driver> findAll() {
        return new ArrayList<>(driverMap.values());
    }

    public List<Driver> findByStatus(String status) {
        return driverMap.values().stream()
                .filter(driver -> status.equals(driver.getStatus()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        driverMap.remove(id);
    }

    public boolean existsById(String id) {
        return driverMap.containsKey(id);
    }

    public long count() {
        return driverMap.size();
    }

    public long countByStatus(String status) {
        return driverMap.values().stream()
                .filter(driver -> status.equals(driver.getStatus()))
                .count();
    }
}
