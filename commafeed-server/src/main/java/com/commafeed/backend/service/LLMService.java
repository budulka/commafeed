package com.commafeed.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Singleton
public class LLMService {

    @Inject ObjectMapper objectMapper;

    /**
     * Generate an alternative text given source and a prompt. Provider is selected via LLM_PROVIDER
     * env.
     */
    public String generateAlternative(String source, String prompt) throws LLMException {
        String provider = System.getenv("LLM_PROVIDER");
        String apiUrl = System.getenv("LLM_API_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String model = System.getenv("LLM_MODEL");
        String input = (prompt == null ? "" : prompt) + "\n\n" + (source == null ? "" : source);

        // No provider or URL -> safe local fallback
        if (provider == null || provider.isEmpty() || apiUrl == null || apiUrl.isEmpty()) {
            return "[fallback] " + (source == null ? "" : source);
        }

        try {
            if ("groq".equalsIgnoreCase(provider)) {
                return callGroq(apiUrl, apiKey, model, input);
            }

            // Unknown provider -> fail fast
            throw new LLMException("unsupported llm provider: " + provider);
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("LLM call failed", e);
        }
    }

    private String callGroq(String apiUrl, String apiKey, String model, String input)
            throws LLMException {
        try {
            HttpClient client =
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            if (model == null || model.isEmpty()) {
                model = "llama-3.1-8b-instant";
            }

            Map<String, Object> payload =
                    Map.of(
                            "model",
                            model,
                            "messages",
                            List.of(Map.of("role", "user", "content", input)));
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest.Builder reqb =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .timeout(Duration.ofSeconds(30))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json));

            if (apiKey != null && !apiKey.isEmpty()) {
                reqb.header("Authorization", "Bearer " + apiKey);
            }

            HttpRequest request = reqb.build();
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                Map<String, Object> map =
                        objectMapper.readValue(resp.body(), new TypeReference<>() {});
                Object choicesObj = map.get("choices");
                if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                    Object firstChoiceObj = choices.get(0);
                    if (firstChoiceObj instanceof Map<?, ?> firstChoice) {
                        Object messageObj = firstChoice.get("message");
                        if (messageObj instanceof Map<?, ?> message) {
                            Object contentObj = message.get("content");
                            if (contentObj instanceof String content) {
                                return content;
                            }
                        }
                    }
                }
                return resp.body();
            }

            System.out.println("[DEBUG_LOG] LLM ERROR STATUS: " + resp.statusCode());
            System.out.println("[DEBUG_LOG] LLM ERROR BODY: " + resp.body());
            throw new LLMException("LLM returned status " + resp.statusCode() + ": " + resp.body());
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("groq call failed", e);
        }
    }

    public static class LLMException extends Exception {
        private static final long serialVersionUID = 1L;

        public LLMException(String msg) {
            super(msg);
        }

        public LLMException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
