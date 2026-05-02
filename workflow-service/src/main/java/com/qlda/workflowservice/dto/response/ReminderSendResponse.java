package com.qlda.workflowservice.dto.response;

import java.util.List;

public record ReminderSendResponse(
        int totalSent,
        List<String> kenhGui
) {
}
