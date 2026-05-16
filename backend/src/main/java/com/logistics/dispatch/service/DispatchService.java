package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Driver;
import com.logistics.dispatch.model.Order;
import com.logistics.dispatch.model.TransportBatch;
import com.logistics.dispatch.model.Vehicle;
import com.logistics.dispatch.repository.BatchRepository;
import com.logistics.dispatch.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import com.logistics.dispatch.model.TransportNode;

@Service
public class DispatchService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private BatchRepository batchRepository;
    @Autowired
    private DriverRepository driverRepository;

    public Map<String, Object> matchVehicle(String region, Double weight, Double volume) {
        Map<String, Object> result = new HashMap<>();
        
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles(region, weight, volume);
        List<Order> pendingOrders = orderService.getOrdersByStatus("PENDING");
        
        if (region != null) {
            pendingOrders = pendingOrders.stream()
                    .filter(o -> region.equals(o.getRegion()))
                    .collect(Collectors.toList());
        }
        
        result.put("availableVehicles", availableVehicles);
        result.put("pendingOrders", pendingOrders);
        result.put("vehicleCount", availableVehicles.size());
        result.put("orderCount", pendingOrders.size());
        
        return result;
    }

    public List<TransportBatch> mergeOrders(List<String> orderIds, String vehicleId, String driverId) {
        List<Order> orders = new ArrayList<>();
        double totalWeight = 0;
        double totalVolume = 0;
        String region = null;
        
        for (String orderId : orderIds) {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if ("PENDING".equals(order.getStatus())) {
                    orders.add(order);
                    totalWeight += order.getWeight();
                    totalVolume += order.getVolume();
                    if (region == null) region = order.getRegion();
                }
            }
        }
        
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            if (totalWeight > vehicle.getMaxWeight() || totalVolume > vehicle.getMaxVolume()) {
                throw new RuntimeException("车辆容量不足！");
            }
        }
        
        Map<String, List<Order>> routeGroups = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStartAddress() + "_" + o.getEndAddress()));
        
        List<TransportBatch> batches = new ArrayList<>();
        
        for (Map.Entry<String, List<Order>> entry : routeGroups.entrySet()) {
            List<Order> routeOrders = entry.getValue();
            
            TransportBatch batch = new TransportBatch();
            batch.setOrderIds(routeOrders.stream().map(Order::getId).collect(Collectors.toList()));
            batch.setVehicleId(vehicleId);
            batch.setDriverId(driverId);
            batch.setRegion(region);
            batch.setStartAddress(routeOrders.get(0).getStartAddress());
            batch.setEndAddress(routeOrders.get(0).getEndAddress());
            batch.setRoute(routeOrders.get(0).getStartAddress() + " -> " + routeOrders.get(0).getEndAddress());
            batch.setTotalWeight(routeOrders.stream().mapToDouble(Order::getWeight).sum());
            batch.setTotalVolume(routeOrders.stream().mapToDouble(Order::getVolume).sum());
            batch.setStatus("ASSIGNED");
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 8);
            batch.setExpectedArrivalTime(cal.getTime());
            
            batch = batchRepository.save(batch);
            batches.add(batch);
            
            for (Order order : routeOrders) {
                order.setBatchId(batch.getId());
                order.setVehicleId(vehicleId);
                order.setDriverId(driverId);
                order.setStatus("ASSIGNED");
                orderService.updateOrder(order);
            }
            
            vehicleService.updateVehicleStatus(vehicleId, "BUSY");
        }
        
        return batches;
    }

    public List<TransportBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public List<TransportBatch> getBatchesByStatus(String status) {
        return batchRepository.findByStatus(status);
    }

    public Optional<TransportBatch> getBatchById(String id) {
        return batchRepository.findById(id);
    }

    public TransportBatch updateBatchStatus(String id, String status) {
        Optional<TransportBatch> batchOpt = batchRepository.findById(id);
        if (batchOpt.isPresent()) {
            TransportBatch batch = batchOpt.get();
            batch.setStatus(status);
            return batchRepository.save(batch);
        }
        return null;
    }

    public void initMockData() {
        List<Order> pendingOrders = orderService.getOrdersByStatus("PENDING");
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles(null, null, null);
        List<Driver> availableDrivers = driverService.getDriversByStatus("AVAILABLE");

        if (pendingOrders.isEmpty() || availableVehicles.isEmpty() || availableDrivers.isEmpty()) {
            return;
        }

        String[] routes = {"上海_北京", "广州_成都", "深圳_武汉", "杭州_西安"};
        
        for (int i = 0; i < 3 && i < availableVehicles.size() && i < availableDrivers.size(); i++) {
            Vehicle vehicle = availableVehicles.get(i);
            Driver driver = availableDrivers.get(i);
            String route = routes[i % routes.length];
            String startAddr = route.split("_")[0];
            String endAddr = route.split("_")[1];
            
            List<Order> routeOrders = new ArrayList<>();
            double totalWeight = 0;
            double totalVolume = 0;
            
            for (Order order : pendingOrders) {
                if (routeOrders.size() >= 2) break;
                if (order.getStartAddress().contains(startAddr) || order.getEndAddress().contains(endAddr)) {
                    routeOrders.add(order);
                    totalWeight += order.getWeight();
                    totalVolume += order.getVolume();
                }
            }
            
            if (routeOrders.isEmpty()) {
                for (int j = 0; j < Math.min(2, pendingOrders.size()); j++) {
                    routeOrders.add(pendingOrders.get(j));
                    totalWeight += pendingOrders.get(j).getWeight();
                    totalVolume += pendingOrders.get(j).getVolume();
                }
            }

            TransportBatch batch = new TransportBatch();
            batch.setOrderIds(routeOrders.stream().map(Order::getId).collect(Collectors.toList()));
            batch.setVehicleId(vehicle.getId());
            batch.setDriverId(driver.getId());
            batch.setRegion(vehicle.getRegion());
            batch.setStartAddress(startAddr);
            batch.setEndAddress(endAddr);
            batch.setRoute(startAddr + " -> " + endAddr);
            batch.setTotalWeight(totalWeight);
            batch.setTotalVolume(totalVolume);
            
            if (i == 0) {
                batch.setStatus("ASSIGNED");
            } else if (i == 1) {
                batch.setStatus("IN_TRANSIT");
                vehicle.setStatus("TRANSPORTING");
                driver.setStatus("TRANSPORTING");
                driverRepository.save(driver);
                vehicleService.updateVehicle(vehicle);
                
                TransportNode node1 = new TransportNode();
                node1.setNodeName("已离开发货地");
                node1.setLocation(startAddr + "物流中心");
                node1.setArrivalTime(new Date(System.currentTimeMillis() - 3600000));
                node1.setRemark("货物已装车，准备出发");
                
                TransportNode node2 = new TransportNode();
                node2.setNodeName("运输途中 - 中转站A");
                node2.setLocation("京沪高速服务区");
                node2.setArrivalTime(new Date());
                node2.setRemark("中途停靠休息");
                
                batch.setNodeHistory(Arrays.asList(node1, node2));
            } else {
                batch.setStatus("COMPLETED");
                
                TransportNode node1 = new TransportNode();
                node1.setNodeName("已离开发货地");
                node1.setLocation(startAddr + "仓库");
                node1.setArrivalTime(new Date(System.currentTimeMillis() - 86400000));
                
                TransportNode node2 = new TransportNode();
                node2.setNodeName("到达目的地");
                node2.setLocation(endAddr + "配送中心");
                node2.setArrivalTime(new Date(System.currentTimeMillis() - 43200000));
                
                batch.setNodeHistory(Arrays.asList(node1, node2));
            }
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 8);
            batch.setExpectedArrivalTime(cal.getTime());
            
            if (i == 1) {
                batch.setStartTime(new Date(System.currentTimeMillis() - 3600000));
            }
            if (i == 2) {
                batch.setStartTime(new Date(System.currentTimeMillis() - 86400000));
                batch.setCompleteTime(new Date(System.currentTimeMillis() - 43200000));
            }
            
            batchRepository.save(batch);
            
            for (int j = 0; j < routeOrders.size(); j++) {
                Order order = routeOrders.get(j);
                order.setBatchId(batch.getId());
                order.setVehicleId(vehicle.getId());
                order.setDriverId(driver.getId());
                order.setLoadingOrder(j + 1);
                if (i == 0) {
                    order.setStatus("ASSIGNED");
                } else if (i == 1) {
                    order.setStatus("IN_TRANSIT");
                } else {
                    order.setStatus("COMPLETED");
                }
                orderService.updateOrder(order);
            }
            
            if (i == 0) {
                vehicleService.updateVehicleStatus(vehicle.getId(), "BUSY");
            }
            
            pendingOrders.removeAll(routeOrders);
        }
    }
}
