package com.portfolio.stocksage.dto.response;

import com.portfolio.stocksage.report.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {

    private Long id;
    private String title;
    private String description;
    private ReportType reportType;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> parameters;
    private List<String> columns;
    private List<Map<String, Object>> data;
    private Map<String, Object> summary;
    private List<ChartData> charts;

    // Initialize collections to avoid NPEs
    {
        parameters = new HashMap<>();
        columns = new ArrayList<>();
        data = new ArrayList<>();
        summary = new HashMap<>();
        charts = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartData {
        private String chartType; // pie, bar, line, etc.
        private String title;
        private String xAxisLabel;
        private String yAxisLabel;
        private List<String> labels;
        private List<DataSeries> series;

        // Initialize collections
        {
            labels = new ArrayList<>();
            series = new ArrayList<>();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSeries {
        private String name;
        private List<Object> data;
        private String color;

        // Initialize collections
        {
            data = new ArrayList<>();
        }
    }
}