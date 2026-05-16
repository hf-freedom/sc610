package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Order;
import com.logistics.dispatch.model.TransportBatch;
import com.logistics.dispatch.model.Vehicle;
import com.logistics.dispatch.repository.BatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransportService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private BatchRepository batchRepository;

    public TransportBatch acceptBatch(String batchId, String driverId) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return null;
        }
        
        TransportBatch batch = batchOpt.get();
        if (!"ASSIGNED".equals(batch.getStatus())) {
            throw new RuntimeException("该批次状态不允许接单");
        }
        
        batch.setDriverId(driverId);
        batch.setStatus("ACCEPTED");
        batch = batchRepository.save(batch);
        
        vehicleService.updateVehicleStatus(batch.getVehicleId(), "TRANSPORTING");
        driverService.updateDriverStatus(driverId, "TRANSPORTING");
        
        for (String orderId : batch.getOrderIds()) {
            orderService.updateOrderStatus(orderId, "TRANSPORTING");
        }
        
        return batch;
    }

    public boolean recordLoading(String batchId, List<String> orderIds) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return false;
        }
        
        int loadingOrder = 1;
        for (String orderId : orderIds) {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setLoadingOrder(loadingOrder++);
                orderService.updateOrder(order);
            }
        }
        
        return true;
    }

    public TransportBatch startTransport(String batchId) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return null;
        }
        
        TransportBatch batch = batchOpt.get();
        batch.setStatus("IN_TRANSIT");
        batch.setStartTime(new Date());
        return batchRepository.save(batch);
    }

    public TransportBatch completeTransport(String batchId) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return null;
        }
        
        TransportBatch batch = batchOpt.get();
        batch.setStatus("COMPLETED");
        batch.setCompleteTime(new Date());
        batchRepository.save(batch);
        
        vehicleService.updateVehicleStatus(batch.getVehicleId(), "AVAILABLE");
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(batch.getVehicleId());
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            vehicle.setCurrentLoadWeight(0.0);
            vehicle.setCurrentLoadVolume(0.0);
            vehicle.setCurrentBatchId(null);
            vehicleService.updateVehicle(vehicle);
        }
        
        driverService.updateDriverStatus(batch.getDriverId(), "AVAILABLE");
        
        boolean isOnTime = batch.getCompleteTime().before(batch.getExpectedArrivalTime());
        driverService.updateOnTimeRate(batch.getDriverId(), isOnTime);
        
        for (String orderId : batch.getOrderIds()) {
            orderService.updateOrderStatus(orderId, "COMPLETED");
        }
        
        return batch;
    }

    public TransportBatch updateNode(String batchId, String nodeName, String location) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return null;
        }
        
        TransportBatch batch = batchOpt.get();
        
        TransportNode node = new TransportNode();
        node.setNodeName(nodeName);
        node.setLocation(location);
        node.setArrivalTime(new Date());
        node.setRemark("节点更新: " + nodeName);
        batch.getNodeHistory().add(node);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 4);
        batch.setExpectedArrivalTime(cal.getTime());
        
        for (String orderId : batch.getOrderIds()) {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setExpectedArrivalTime(cal.getTime());
                orderService.updateOrder(order);
            }
        }
        
        return batchRepository.save(batch);
    }

    public List<TransportBatch> getTransportingBatches() {
        return batchRepository.findByStatus("IN_TRANSIT");
    }
}
