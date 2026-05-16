package com.logistics.dispatch.model;

import lombok.Data;
import java.util.Date;

@Data
public class TransportNode {
    private String id;
    private String batchId;
    private String orderId;
    private String nodeName;
    private String location;
    private Date arrivalTime;
    private Date leaveTime;
    private String status;
    private String remark;
}
