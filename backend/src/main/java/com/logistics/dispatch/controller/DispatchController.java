package com.logistics.dispatch.controller;

import com.logistics.dispatch.model.TransportBatch;
import com.logistics.dispatch.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {
    @Autowired
    private DispatchService dispatchService;

    @GetMapping("/match")
    public ResponseEntity<Map<String, Object>> matchVehicles(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") Double weight,
            @RequestParam(defaultValue = "0") Double volume) {
        return ResponseEntity.ok(dispatchService.matchVehicle(region, weight, volume));
    }

    @PostMapping("/merge")
    public ResponseEntity<List<TransportBatch>> mergeOrders(@RequestBody Map<String, Object> body) {
        List<String> orderIds = (List<String>) body.get("orderIds");
        String vehicleId = (String) body.get("vehicleId");
        String driverId = (String) body.get("driverId");
        return ResponseEntity.ok(dispatchService.mergeOrders(orderIds, vehicleId, driverId));
    }

    @GetMapping("/batch")
    public ResponseEntity<List<TransportBatch>> getAllBatches() {
        return ResponseEntity.ok(dispatchService.getAllBatches());
    }

    @GetMapping("/batch/{id}")
    public ResponseEntity<TransportBatch> getBatchById(@PathVariable String id) {
        Optional<TransportBatch> batch = dispatchService.getBatchById(id);
        return batch.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch/status/{status}")
    public ResponseEntity<List<TransportBatch>> getBatchesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(dispatchService.getBatchesByStatus(status));
    }

    @PutMapping("/batch/{id}/status")
    public ResponseEntity<TransportBatch> updateBatchStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        TransportBatch updated = dispatchService.updateBatchStatus(id, status);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
}
