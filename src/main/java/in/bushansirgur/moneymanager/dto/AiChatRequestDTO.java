package in.bushansirgur.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiChatRequestDTO {
    private String message;
    private List<ChatMessageDTO> history;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChatMessageDTO {
        private String role;
        private String content;
    }
}
