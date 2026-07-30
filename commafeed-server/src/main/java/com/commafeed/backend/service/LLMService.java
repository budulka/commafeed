package com.commafeed.backend.service;

import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Singleton
public class LLMService {

    public static class LLMException extends Exception {
        public LLMException(String msg) { super(msg); }
        public LLMException(String msg, Throwable t) { super(msg, t); }
    }

    public String generateAlternative(String source, String prompt) throws LLMException {
        String apiUrl = System.getenv("LLM_API_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String input = (prompt == null ? "" : prompt) + "\n\n" + (source == null ? "" : source);

        if (apiUrl == null || apiUrl.isEmpty()) {
            // Fallback when no external LLM is configured: return a simple, non-sensitive rewrite
            String fallback = "[fallback] " + (source == null ? "" : source);
            return fallback;
        }

        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest.Builder reqb =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .timeout(Duration.ofSeconds(30))
                            .header("Content-Type", "text/plain")
                            .POST(HttpRequest.BodyPublishers.ofString(input));

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
            throw new LLMException("LLM call failed", e);
        }
    }
}
