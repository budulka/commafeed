package com.commafeed.backend.service;

import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Singleton
public class LLMService {

    /**
     * Generate an alternative text given source and a prompt. Provider is selected via LLM_PROVIDER env.
     */
    public String generateAlternative(String source, String prompt) throws LLMException {
        String provider = System.getenv("LLM_PROVIDER");
        String apiUrl = System.getenv("LLM_API_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String input = (prompt == null ? "" : prompt) + "\n\n" + (source == null ? "" : source);

        // No provider or URL -> safe local fallback
        if (provider == null || provider.isEmpty() || apiUrl == null || apiUrl.isEmpty()) {
            return "[fallback] " + (source == null ? "" : source);
        }

        try {
            if ("groq".equalsIgnoreCase(provider)) {
                return callGroq(apiUrl, apiKey, input);
            }

            // Unknown provider -> fail fast
            throw new LLMException("unsupported llm provider: " + provider);
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("LLM call failed", e);
        }
    }

    private String callGroq(String apiUrl, String apiKey, String input) throws LLMException {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            String json = "{\"prompt\":\"" + escapeJson(input) + "\"}";

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
                return resp.body();
            }

            throw new LLMException("LLM returned status " + resp.statusCode());
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("groq call failed", e);
        }
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static class LLMException extends Exception {
        public LLMException(String msg) {
            super(msg);
        }

        public LLMException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
