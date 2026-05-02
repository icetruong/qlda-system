package com.qlda.notificationservice.report.dto;

import java.util.List;

public record DocumentStatisticsResponse(
    String groupBy,
    List<StatisticItem> items
) {
}

