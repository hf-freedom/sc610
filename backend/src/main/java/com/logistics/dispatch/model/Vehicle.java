package com.logistics.dispatch.model;

import lombok.Data;
import java.util.Date;

@Data
public class Vehicle {
    private String id;
    private String plateNumber;
    private String model;
    private Double maxWeight;
    private Double maxVolume;
    private String region;
    private String status;
    private Double currentLoadWeight;
    private Double currentLoadVolume;
    private String currentBatchId;
    private Date createTime;
    private Date lastMaintenanceTime;
}
