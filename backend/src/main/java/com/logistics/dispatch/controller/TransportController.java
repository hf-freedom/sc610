package com.logistics.dispatch.controller;

import com.logistics.dispatch.model.TransportBatch;
import com.logistics.dispatch.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transport")
public class TransportController {
    @Autowired
    private TransportService transportService;

    @PostMapping("/accept")
    public ResponseEntity<TransportBatch> acceptBatch(@RequestBody Map<String, String> body) {
        String batchId = body.get("batchId");
        String driverId = body.get("driverId");
        TransportBatch batch = transportService.acceptBatch(batchId, driverId);
        return batch != null ? ResponseEntity.ok(batch) : ResponseEntity.notFound().build();
    }

    @PostMapping("/loading")
    public ResponseEntity<Map<String, Boolean>> recordLoading(@RequestBody Map<String, Object> body) {
        String batchId = (String) body.get("batchId");
        List<String> orderIds = (List<String>) body.get("orderIds");
        boolean success = transportService.recordLoading(batchId, orderIds);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<TransportBatch> startTransport(@RequestBody Map<String, String> body) {
        String batchId = body.get("batchId");
        TransportBatch batch = transportService.startTransport(batchId);
        return batch != null ? ResponseEntity.ok(batch) : ResponseEntity.notFound().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<TransportBatch> completeTransport(@RequestBody Map<String, String> body) {
        String batchId = body.get("batchId");
        TransportBatch batch = transportService.completeTransport(batchId);
        return batch != null ? ResponseEntity.ok(batch) : ResponseEntity.notFound().build();
    }

    @PostMapping("/node")
    public ResponseEntity<TransportBatch> updateNode(@RequestBody Map<String, String> body) {
        String batchId = body.get("batchId");
        String nodeName = body.get("nodeName");
        String location = body.get("location");
        TransportBatch batch = transportService.updateNode(batchId, nodeName, location);
        return batch != null ? ResponseEntity.ok(batch) : ResponseEntity.notFound().build();
    }

    @GetMapping("/transporting")
    public ResponseEntity<List<TransportBatch>> getTransportingBatches() {
        return ResponseEntity.ok(transportService.getTransportingBatches());
    }
}
