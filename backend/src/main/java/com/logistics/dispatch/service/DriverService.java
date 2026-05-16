package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Driver;
import com.logistics.dispatch.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;

    public Driver createDriver(Driver driver) {
        driver.setStatus("AVAILABLE");
        driver.setOnTimeRate(100.0);
        driver.setTotalOrders(0);
        driver.setOnTimeOrders(0);
        return driverRepository.save(driver);
    }

    public Optional<Driver> getDriverById(String id) {
        return driverRepository.findById(id);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public List<Driver> getDriversByStatus(String status) {
        return driverRepository.findByStatus(status);
    }

    public Driver updateDriverStatus(String id, String status) {
        Optional<Driver> driverOpt = driverRepository.findById(id);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setStatus(status);
            return driverRepository.save(driver);
        }
        return null;
    }

    public Driver updateDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public void updateOnTimeRate(String driverId, boolean isOnTime) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setTotalOrders(driver.getTotalOrders() + 1);
            if (isOnTime) {
                driver.setOnTimeOrders(driver.getOnTimeOrders() + 1);
            }
            double rate = (double) driver.getOnTimeOrders() / driver.getTotalOrders() * 100;
            driver.setOnTimeRate(Math.round(rate * 100.0) / 100.0);
            driverRepository.save(driver);
        }
    }

    public boolean deleteDriver(String id) {
        if (driverRepository.existsById(id)) {
            driverRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void initMockData() {
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
        String[] phones = {"13800138001", "13800138002", "13800138003", "13800138004", "13800138005"};

        for (int i = 0; i < 8; i++) {
            Driver driver = new Driver();
            driver.setName(names[i % names.length]);
            driver.setPhone(phones[i % phones.length].substring(0, 7) + String.format("%04d", i + 100));
            driver.setStatus("AVAILABLE");
            driverRepository.save(driver);
        }
    }
}
