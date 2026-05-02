package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ReminderCheckDeadlineRequest(
        @NotNull LocalDateTime checkDate,
        @NotNull @PositiveOrZero Integer beforeHours
) {
}
