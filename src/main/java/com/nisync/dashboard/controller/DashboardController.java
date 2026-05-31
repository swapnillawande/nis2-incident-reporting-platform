package com.nisync.dashboard.controller;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.dashboard.service.DashboardService;
import com.nisync.incident.dto.IncidentResponseDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDto getSummary() {
        logger.info("GET /dashboard/summary called");

        return dashboardService.getSummary();
    }

    @GetMapping("/recent-incidents")
    public List<IncidentResponseDto> getRecentActiveIncidents() {
        logger.info("GET /dashboard/recent-incidents called");

        return dashboardService.getRecentActiveIncidents();
    }
}
