package com.logistics.dispatch.config;

import com.logistics.dispatch.service.DispatchService;
import com.logistics.dispatch.service.DriverService;
import com.logistics.dispatch.service.OrderService;
import com.logistics.dispatch.service.VehicleService;
import com.logistics.dispatch.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitConfig implements CommandLineRunner {
    @Autowired
    private OrderService orderService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private WarningService warningService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始初始化模拟数据...");
        orderService.initMockData();
        vehicleService.initMockData();
        driverService.initMockData();
        dispatchService.initMockData();
        warningService.initMockData();
        System.out.println("模拟数据初始化完成！");
    }
}
