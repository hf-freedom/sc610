package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Order;
import com.logistics.dispatch.model.TransportBatch;
import com.logistics.dispatch.model.Vehicle;
import com.logistics.dispatch.model.Warning;
import com.logistics.dispatch.repository.BatchRepository;
import com.logistics.dispatch.repository.WarningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WarningService {
    @Autowired
    private WarningRepository warningRepository;
    @Autowired
    private BatchRepository batchRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DispatchService dispatchService;

    @Scheduled(fixedRate = 60000)
    public void checkDelayedOrders() {
        List<TransportBatch> transportingBatches = batchRepository.findByStatus("IN_TRANSIT");
        Date now = new Date();
        
        for (TransportBatch batch : transportingBatches) {
            if (batch.getExpectedArrivalTime() != null && now.after(batch.getExpectedArrivalTime())) {
                long diffMinutes = (now.getTime() - batch.getExpectedArrivalTime().getTime()) / (60 * 1000);
                
                if (diffMinutes > 30) {
                    boolean existingWarning = warningRepository.findByBatchId(batch.getId()).stream()
                            .anyMatch(w -> "PENDING".equals(w.getStatus()));
                    
                    if (!existingWarning) {
                        Warning warning = new Warning();
                        warning.setBatchId(batch.getId());
                        warning.setType("DELAY");
                        warning.setLevel(diffMinutes > 120 ? "HIGH" : diffMinutes > 60 ? "MEDIUM" : "LOW");
                        warning.setMessage("运输批次延误超过" + diffMinutes + "分钟");
                        warning.setStartAddress(batch.getStartAddress());
                        warning.setEndAddress(batch.getEndAddress());
                        warning.setRegion(batch.getRegion());
                        warning.setTotalWeight(batch.getTotalWeight());
                        warning.setTotalVolume(batch.getTotalVolume());
                        warning.setOrderIds(batch.getOrderIds());
                        warning.setDelayMinutes(diffMinutes);
                        warningRepository.save(warning);
                        
                        for (String orderId : batch.getOrderIds()) {
                            orderService.updateOrderStatus(orderId, "EXCEPTION");
                        }
                    }
                }
            }
        }
    }

    public List<Warning> getAllWarnings() {
        return warningRepository.findAll();
    }

    public List<Warning> getWarningsByStatus(String status) {
        return warningRepository.findByStatus(status);
    }

    public Warning handleWarning(String id, String handler, String remark) {
        Optional<Warning> warningOpt = warningRepository.findById(id);
        if (warningOpt.isPresent()) {
            Warning warning = warningOpt.get();
            warning.setStatus("HANDLED");
            warning.setHandleTime(new Date());
            warning.setHandler(handler);
            warning.setRemark(remark);
            return warningRepository.save(warning);
        }
        return null;
    }

    public Warning createWarning(Warning warning) {
        return warningRepository.save(warning);
    }

    public Map<String, Object> getWarningDetail(String id) {
        Optional<Warning> warningOpt = warningRepository.findById(id);
        if (warningOpt.isPresent()) {
            Warning warning = warningOpt.get();
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("warning", warning);
            
            if (warning.getBatchId() != null) {
                Optional<TransportBatch> batchOpt = batchRepository.findById(warning.getBatchId());
                batchOpt.ifPresent(batch -> result.put("batch", batch));
            }
            
            if (warning.getOrderIds() != null && !warning.getOrderIds().isEmpty()) {
                List<Order> orders = warning.getOrderIds().stream()
                        .map(orderService::getOrderById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                result.put("orders", orders);
            }
            
            List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles(
                    warning.getRegion(), 
                    warning.getTotalWeight(), 
                    warning.getTotalVolume()
            );
            result.put("availableVehicles", availableVehicles);
            
            return result;
        }
        return null;
    }

    public boolean rescheduleVehicle(String warningId, String newVehicleId, String newDriverId, String handler) {
        Optional<Warning> warningOpt = warningRepository.findById(warningId);
        if (!warningOpt.isPresent()) {
            return false;
        }
        
        Warning warning = warningOpt.get();
        String batchId = warning.getBatchId();
        
        Optional<TransportBatch> batchOpt = batchRepository.findById(batchId);
        if (!batchOpt.isPresent()) {
            return false;
        }
        
        TransportBatch batch = batchOpt.get();
        
        Optional<Vehicle> newVehicleOpt = vehicleService.getVehicleById(newVehicleId);
        if (!newVehicleOpt.isPresent()) {
            return false;
        }
        Vehicle newVehicle = newVehicleOpt.get();
        
        if (newVehicle.getMaxWeight() < batch.getTotalWeight() || newVehicle.getMaxVolume() < batch.getTotalVolume()) {
            throw new RuntimeException("车辆容量不足！");
        }
        
        Vehicle oldVehicle = vehicleService.getVehicleById(batch.getVehicleId()).orElse(null);
        if (oldVehicle != null) {
            oldVehicle.setStatus("AVAILABLE");
            oldVehicle.setCurrentBatchId(null);
            vehicleService.updateVehicle(oldVehicle);
        }
        
        newVehicle.setStatus("TRANSPORTING");
        newVehicle.setCurrentBatchId(batchId);
        vehicleService.updateVehicle(newVehicle);
        
        batch.setVehicleId(newVehicleId);
        batch.setDriverId(newDriverId);
        batch.setStatus("REASSIGNED");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.HOUR, 8);
        batch.setExpectedArrivalTime(cal.getTime());
        batchRepository.save(batch);
        
        for (String orderId : batch.getOrderIds()) {
            orderService.updateOrderStatus(orderId, "ASSIGNED");
        }
        
        warning.setStatus("HANDLED");
        warning.setHandleTime(new Date());
        warning.setHandler(handler);
        warning.setRemark("已重新调度车辆：" + newVehicle.getPlateNumber());
        warningRepository.save(warning);
        
        return true;
    }

    public void initMockData() {
        List<TransportBatch> batches = batchRepository.findByStatus("IN_TRANSIT");
        if (batches.isEmpty()) return;
        
        TransportBatch batch = batches.get(0);
        
        Warning warning = new Warning();
        warning.setBatchId(batch.getId());
        warning.setType("DELAY");
        warning.setLevel("HIGH");
        warning.setMessage("运输批次延误超过150分钟");
        warning.setStartAddress(batch.getStartAddress());
        warning.setEndAddress(batch.getEndAddress());
        warning.setRegion(batch.getRegion());
        warning.setTotalWeight(batch.getTotalWeight());
        warning.setTotalVolume(batch.getTotalVolume());
        warning.setOrderIds(batch.getOrderIds());
        warning.setDelayMinutes(150L);
        warning.setStatus("PENDING");
        
        warningRepository.save(warning);
    }
}
