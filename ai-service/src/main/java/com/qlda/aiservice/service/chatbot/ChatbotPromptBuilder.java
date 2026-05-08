package com.qlda.aiservice.service.chatbot;

import org.springframework.stereotype.Component;

@Component
public class ChatbotPromptBuilder {

    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI của hệ thống quản lý văn bản.
        Chỉ trả lời dựa trên dữ liệu được cung cấp trong context.
        Không tự bịa thông tin.
        Nếu không tìm thấy dữ liệu phù hợp, hãy trả lời: "Không tìm thấy dữ liệu phù hợp."
        Trả lời bằng tiếng Việt.
        Trả lời rõ ràng, ngắn gọn, dễ hiểu.
        Không hiển thị thông tin kỹ thuật nội bộ cho người dùng cuối.
        """;

    public String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildDocumentSearchPrompt(String question, String retrievedDocuments) {
        return """
            Nhiệm vụ:
            Người dùng đang muốn tìm tài liệu/văn bản trong hệ thống.

            Câu hỏi người dùng:
            %s

            Dữ liệu tìm được:
            %s

            Hãy trả lời:
            - Tóm tắt ngắn gọn kết quả tìm được.
            - Liệt kê các văn bản phù hợp nhất.
            - Không nhắc đến vector, embedding, chunk.
            - Nếu không có dữ liệu, trả lời "Không tìm thấy tài liệu phù hợp."
            """.formatted(question, retrievedDocuments);
    }

    public String buildStatisticPrompt(String question, ChatbotMetricCode metricCode, long metricResult) {
        return """
            Nhiệm vụ:
            Người dùng đang hỏi số liệu hệ thống.

            Câu hỏi người dùng:
            %s

            Metric:
            %s

            Kết quả truy vấn:
            %d

            Hãy trả lời tự nhiên, rõ ràng, dễ hiểu.
            Không tự suy diễn thêm ngoài dữ liệu được cung cấp.
            """.formatted(question, metricCode.name(), metricResult);
    }

    public String buildUserGuidePrompt(String question, String guideContext) {
        return """
            Nhiệm vụ:
            Người dùng đang hỏi hướng dẫn sử dụng hệ thống.

            Câu hỏi người dùng:
            %s

            Tài liệu hướng dẫn tìm được:
            %s

            Hãy hướng dẫn từng bước ngắn gọn, dễ hiểu.
            Nếu không có dữ liệu hướng dẫn, trả lời "Không tìm thấy hướng dẫn phù hợp."
            """.formatted(question, guideContext);
    }
}
