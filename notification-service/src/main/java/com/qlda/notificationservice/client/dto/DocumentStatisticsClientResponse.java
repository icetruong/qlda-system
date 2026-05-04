package com.qlda.notificationservice.client.dto;

import java.util.List;

public record DocumentStatisticsClientResponse(
    Integer totalDocuments,
    Integer incomingDocuments,
    Integer outgoingDocuments,
    List<StatisticClientItem> items
) {
}
