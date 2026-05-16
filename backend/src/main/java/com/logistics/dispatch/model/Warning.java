package com.logistics.dispatch.model;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Warning {
    private String id;
    private String batchId;
    private String orderId;
    private String type;
    private String level;
    private String message;
    private String status;
    private Date createTime;
    private Date handleTime;
    private String handler;
    private String remark;
    private String startAddress;
    private String endAddress;
    private String region;
    private Double totalWeight;
    private Double totalVolume;
    private List<String> orderIds;
    private Long delayMinutes;
}
