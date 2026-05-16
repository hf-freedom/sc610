package com.logistics.dispatch.repository;

import com.logistics.dispatch.model.TransportBatch;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class BatchRepository {
    private final Map<String, TransportBatch> batchMap = new ConcurrentHashMap<>();
    private int batchCounter = 1;

    public TransportBatch save(TransportBatch batch) {
        if (batch.getId() == null) {
            batch.setId(UUID.randomUUID().toString());
            batch.setBatchNo("BATCH" + String.format("%06d", batchCounter++));
            batch.setCreateTime(new Date());
        }
        batchMap.put(batch.getId(), batch);
        return batch;
    }

    public Optional<TransportBatch> findById(String id) {
        return Optional.ofNullable(batchMap.get(id));
    }

    public List<TransportBatch> findAll() {
        return new ArrayList<>(batchMap.values());
    }

    public List<TransportBatch> findByStatus(String status) {
        return batchMap.values().stream()
                .filter(batch -> status.equals(batch.getStatus()))
                .collect(Collectors.toList());
    }

    public List<TransportBatch> findByVehicleId(String vehicleId) {
        return batchMap.values().stream()
                .filter(batch -> vehicleId.equals(batch.getVehicleId()))
                .collect(Collectors.toList());
    }

    public List<TransportBatch> findByDriverId(String driverId) {
        return batchMap.values().stream()
                .filter(batch -> driverId.equals(batch.getDriverId()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        batchMap.remove(id);
    }

    public boolean existsById(String id) {
        return batchMap.containsKey(id);
    }

    public long count() {
        return batchMap.size();
    }

    public long countByStatus(String status) {
        return batchMap.values().stream()
                .filter(batch -> status.equals(batch.getStatus()))
                .count();
    }
}
