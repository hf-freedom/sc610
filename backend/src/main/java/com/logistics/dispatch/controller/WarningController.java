package com.logistics.dispatch.controller;

import com.logistics.dispatch.model.Warning;
import com.logistics.dispatch.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/warning")
public class WarningController {
    @Autowired
    private WarningService warningService;

    @GetMapping
    public ResponseEntity<List<Warning>> getAllWarnings() {
        return ResponseEntity.ok(warningService.getAllWarnings());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Warning>> getWarningsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(warningService.getWarningsByStatus(status));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getWarningDetail(@PathVariable String id) {
        Map<String, Object> detail = warningService.getWarningDetail(id);
        return detail != null ? ResponseEntity.ok(detail) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/handle")
    public ResponseEntity<Warning> handleWarning(@PathVariable String id, @RequestBody Map<String, String> body) {
        String handler = body.get("handler");
        String remark = body.get("remark");
        Warning updated = warningService.handleWarning(id, handler, remark);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<Map<String, Boolean>> rescheduleVehicle(@PathVariable String id, @RequestBody Map<String, String> body) {
        String newVehicleId = body.get("vehicleId");
        String newDriverId = body.get("driverId");
        String handler = body.get("handler");
        boolean success = warningService.rescheduleVehicle(id, newVehicleId, newDriverId, handler);
        Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Warning> createWarning(@RequestBody Warning warning) {
        return ResponseEntity.ok(warningService.createWarning(warning));
    }
}
