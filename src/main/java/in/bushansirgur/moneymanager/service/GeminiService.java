package in.bushansirgur.moneymanager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI text generation via Groq (OpenAI-compatible chat completions).
 * Kept class name GeminiService so existing AiService wiring stays unchanged.
 */
@Service
@Slf4j
public class GeminiService {

    @Value("${groq.api.key:${gemini.api.key:}}")
    private String apiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "AI is not configured. Please set groq.api.key in application.properties.";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.4);
            body.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            String text = extractText(response.getBody());
            log.info("Groq response: {}", text.length() > 300 ? text.substring(0, 300) + "..." : text);
            return text;
        } catch (Exception e) {
            log.error("Groq API call failed", e);
            return "Unable to generate AI response right now. Please try again later. Error: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return "No response from AI service.";
        }

        if (responseBody.containsKey("error")) {
            return "Groq error: " + responseBody.get("error");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "No AI response generated.";
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            return "No AI response generated.";
        }

        Object content = message.get("content");
        return content != null ? content.toString().trim() : "No AI response generated.";
    }
}
