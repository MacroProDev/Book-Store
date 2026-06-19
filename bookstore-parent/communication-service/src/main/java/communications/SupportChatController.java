package communications;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SupportChatController {

    private final GeminiService geminiService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/support/message")
    public void handleSupportMessage(SupportChatMessage message) {
        message.setType(SupportChatMessage.MessageType.CLIENT_MESSAGE);
        message.setSender("CLIENT");

        // Llamar a Gemini de forma reactiva
        geminiService.generateResponse(message.getMessage())
            .subscribe(responseText -> {
                SupportChatMessage supportResponse = new SupportChatMessage(
                    message.getClientId(),
                    responseText,
                    SupportChatMessage.MessageType.SUPPORT_RESPONSE,
                    "SUPPORT_AGENT"
                );
                messagingTemplate.convertAndSend("/topic/support/123456", supportResponse);
            });
    }
}