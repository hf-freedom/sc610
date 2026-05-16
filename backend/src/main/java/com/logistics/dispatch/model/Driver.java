package com.logistics.dispatch.model;

import lombok.Data;
import java.util.Date;

@Data
public class Driver {
    private String id;
    private String name;
    private String phone;
    private String status;
    private String currentBatchId;
    private Double onTimeRate;
    private Integer totalOrders;
    private Integer onTimeOrders;
    private Date createTime;
}
