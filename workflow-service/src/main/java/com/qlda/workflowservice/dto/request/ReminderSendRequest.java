package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReminderSendRequest(
        @NotEmpty List<Long> processingIds,
        List<String> kenhGui,
        @Size(max = 1000) String noiDung
) {
}
