package com.logistics.dispatch.controller;

import com.logistics.dispatch.model.Statistics;
import com.logistics.dispatch.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public ResponseEntity<Statistics> getOverview() {
        return ResponseEntity.ok(statisticsService.getOverview());
    }

    @GetMapping("/order-status")
    public ResponseEntity<Map<String, Long>> getOrderStatusDistribution() {
        return ResponseEntity.ok(statisticsService.getOrderStatusDistribution());
    }

    @GetMapping("/region")
    public ResponseEntity<Map<String, Long>> getRegionDistribution() {
        return ResponseEntity.ok(statisticsService.getRegionDistribution());
    }
}
