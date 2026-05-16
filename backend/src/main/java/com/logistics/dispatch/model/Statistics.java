package com.logistics.dispatch.model;

import lombok.Data;

@Data
public class Statistics {
    private Long totalOrders;
    private Long pendingOrders;
    private Long transportingOrders;
    private Long completedOrders;
    private Long exceptionOrders;
    private Long totalVehicles;
    private Long availableVehicles;
    private Long busyVehicles;
    private Long totalDrivers;
    private Long availableDrivers;
    private Double vehicleUtilizationRate;
    private Double driverOnTimeRate;
    private Double routeDelayRate;
}
