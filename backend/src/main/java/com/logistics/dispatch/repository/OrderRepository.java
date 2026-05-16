package com.logistics.dispatch.repository;

import com.logistics.dispatch.model.Order;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();
    private int orderCounter = 1;

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(UUID.randomUUID().toString());
            order.setOrderNo("ORD" + String.format("%06d", orderCounter++));
            order.setCreateTime(new Date());
        }
        orderMap.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orderMap.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(orderMap.values());
    }

    public List<Order> findByStatus(String status) {
        return orderMap.values().stream()
                .filter(order -> status.equals(order.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Order> findByRegion(String region) {
        return orderMap.values().stream()
                .filter(order -> region.equals(order.getRegion()))
                .collect(Collectors.toList());
    }

    public List<Order> findByBatchId(String batchId) {
        return orderMap.values().stream()
                .filter(order -> batchId.equals(order.getBatchId()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        orderMap.remove(id);
    }

    public boolean existsById(String id) {
        return orderMap.containsKey(id);
    }

    public long count() {
        return orderMap.size();
    }

    public long countByStatus(String status) {
        return orderMap.values().stream()
                .filter(order -> status.equals(order.getStatus()))
                .count();
    }
}
