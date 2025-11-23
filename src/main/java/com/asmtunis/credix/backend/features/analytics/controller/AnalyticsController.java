package com.asmtunis.credix.backend.features.analytics.controller;

import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import com.asmtunis.credix.backend.features.analytics.dto.response.DashboardAnalyticsResponse;
import com.asmtunis.credix.backend.features.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	/**
	 * Get dashboard analytics for ADMIN or SUPER_ADMIN
	 * Returns data for line chart, bar chart, pie chart, and doughnut chart
	 */
	@GetMapping("/dashboard")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<ResponseWrapper<DashboardAnalyticsResponse>> getDashboardAnalytics(
			Authentication authentication
	) {
		String userEmail = authentication.getName();
		DashboardAnalyticsResponse analytics = analyticsService.getDashboardAnalytics(userEmail);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "Analytics retrieved successfully", analytics));
	}
}
