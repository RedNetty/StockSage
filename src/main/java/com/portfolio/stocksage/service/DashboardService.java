package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.response.DashboardDTO;

import java.time.LocalDateTime;

public interface DashboardService {

    DashboardDTO getDashboardSummary();

    DashboardDTO getDashboardSummaryForDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
