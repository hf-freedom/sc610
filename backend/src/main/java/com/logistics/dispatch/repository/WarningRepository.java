package com.logistics.dispatch.repository;

import com.logistics.dispatch.model.Warning;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WarningRepository {
    private final Map<String, Warning> warningMap = new ConcurrentHashMap<>();

    public Warning save(Warning warning) {
        if (warning.getId() == null) {
            warning.setId(UUID.randomUUID().toString());
            warning.setCreateTime(new Date());
            warning.setStatus("PENDING");
        }
        warningMap.put(warning.getId(), warning);
        return warning;
    }

    public Optional<Warning> findById(String id) {
        return Optional.ofNullable(warningMap.get(id));
    }

    public List<Warning> findAll() {
        return new ArrayList<>(warningMap.values());
    }

    public List<Warning> findByStatus(String status) {
        return warningMap.values().stream()
                .filter(warning -> status.equals(warning.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Warning> findByOrderId(String orderId) {
        return warningMap.values().stream()
                .filter(warning -> orderId.equals(warning.getOrderId()))
                .collect(Collectors.toList());
    }

    public List<Warning> findByBatchId(String batchId) {
        return warningMap.values().stream()
                .filter(warning -> batchId.equals(warning.getBatchId()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        warningMap.remove(id);
    }

    public boolean existsById(String id) {
        return warningMap.containsKey(id);
    }

    public long count() {
        return warningMap.size();
    }

    public long countByStatus(String status) {
        return warningMap.values().stream()
                .filter(warning -> status.equals(warning.getStatus()))
                .count();
    }
}
