package com.logistics.dispatch.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TransportBatch {
    private String id;
    private String batchNo;
    private String vehicleId;
    private String driverId;
    private List<String> orderIds;
    private String startAddress;
    private String endAddress;
    private String route;
    private String region;
    private String status;
    private Double totalWeight;
    private Double totalVolume;
    private Date createTime;
    private Date startTime;
    private Date expectedArrivalTime;
    private Date completeTime;
    private List<TransportNode> nodeHistory = new ArrayList<>();
}
