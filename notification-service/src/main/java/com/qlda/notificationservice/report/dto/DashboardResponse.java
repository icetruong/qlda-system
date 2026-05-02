package com.qlda.notificationservice.report.dto;

public record DashboardResponse(
    int totalDocuments,
    int incomingDocuments,
    int outgoingDocuments,
    int completedDocuments,
    int processingDocuments,
    int overdueDocuments,
    double completionRate,
    double overdueRate
) {
}

