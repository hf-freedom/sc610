package com.logistics.dispatch.model;

import lombok.Data;
import java.util.Date;

@Data
public class Order {
    private String id;
    private String orderNo;
    private String goodsName;
    private Double weight;
    private Double volume;
    private String startAddress;
    private String endAddress;
    private String region;
    private String status;
    private String batchId;
    private String driverId;
    private String vehicleId;
    private Date createTime;
    private Date expectedArrivalTime;
    private Date actualArrivalTime;
    private Integer loadingOrder;
    private String remark;
}
