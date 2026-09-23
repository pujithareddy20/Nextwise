package com.supportdesk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.dto.ai.AiClassifyTicketRequest;
import com.supportdesk.dto.ai.AiClassifyTicketResponse;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.exception.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(AiClassificationService.class);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${app.ai.provider:gemini}")
    private String configuredProvider;

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-3.5-flash-lite}")
    private String geminiModel;

    @Value("${app.ai.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    @Value("${app.ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${app.ai.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${app.ai.timeout-seconds:15}")
    private int timeoutSeconds;

    public AiClassificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(15));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public String getEffectiveGeminiKey() {
        if (StringUtils.hasText(geminiApiKey)) {
            return geminiApiKey.trim();
        }
        String sysProp = System.getProperty("GEMINI_API_KEY");
        if (StringUtils.hasText(sysProp)) {
            return sysProp.trim();
        }
        String envVar = System.getenv("GEMINI_API_KEY");
        if (StringUtils.hasText(envVar)) {
            return envVar.trim();
        }
        return "";
    }

    public String getEffectiveOpenaiKey() {
        if (StringUtils.hasText(openaiApiKey)) {
            return openaiApiKey.trim();
        }
        String sysProp = System.getProperty("OPENAI_API_KEY");
        if (StringUtils.hasText(sysProp)) {
            return sysProp.trim();
        }
        String envVar = System.getenv("OPENAI_API_KEY");
        if (StringUtils.hasText(envVar)) {
            return envVar.trim();
        }
        return "";
    }

    public Map<String, Object> getStatus() {
        boolean hasGemini = StringUtils.hasText(getEffectiveGeminiKey());
        boolean hasOpenai = StringUtils.hasText(getEffectiveOpenaiKey());

        String provider = "none";
        if ("openai".equalsIgnoreCase(configuredProvider) && hasOpenai) {
            provider = "openai";
        } else if (hasGemini) {
            provider = "gemini";
        } else if (hasOpenai) {
            provider = "openai";
        }

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("configured", hasGemini || hasOpenai);
        status.put("activeProvider", provider);
        status.put("hasGeminiKey", hasGemini);
        status.put("hasOpenaiKey", hasOpenai);
        status.put("geminiModel", geminiModel);
        status.put("openaiModel", openaiModel);
        return status;
    }

    public AiClassifyTicketResponse classifyTicket(AiClassifyTicketRequest request) {
        String provider = resolveProvider();
        logger.info("Executing AI classification for ticket title='{}' using provider '{}'", request.getTitle(), provider);

        try {
            String rawJson;
            if ("openai".equalsIgnoreCase(provider)) {
                rawJson = callOpenAi(request.getTitle(), request.getDescription());
            } else {
                rawJson = callGemini(request.getTitle(), request.getDescription());
            }

            return parseAndSanitizeResponse(rawJson);
        } catch (AiServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error executing AI classification: {}", ex.getMessage(), ex);
            throw new AiServiceUnavailableException("AI service failed to process ticket: " + ex.getMessage(), ex);
        }
    }

    private String resolveProvider() {
        boolean hasGemini = StringUtils.hasText(getEffectiveGeminiKey());
        boolean hasOpenai = StringUtils.hasText(getEffectiveOpenaiKey());

        if (!hasGemini && !hasOpenai) {
            throw new AiServiceUnavailableException(
                    "AI classification service is not configured. Please set GEMINI_API_KEY or OPENAI_API_KEY in the environment."
            );
        }

        if ("openai".equalsIgnoreCase(configuredProvider) && hasOpenai) {
            return "openai";
        }

        if (hasGemini) {
            return "gemini";
        }

        if (hasOpenai) {
            return "openai";
        }

        throw new AiServiceUnavailableException(
                "AI classification service is not configured. Please set GEMINI_API_KEY or OPENAI_API_KEY in the environment."
        );
    }

    private String callGemini(String title, String description) {
        String apiKey = getEffectiveGeminiKey();
        String prompt = buildPrompt(title, description);

        List<String> modelsToTry = new ArrayList<>();
        if (StringUtils.hasText(geminiModel)) {
            modelsToTry.add(geminiModel.trim());
        }
        for (String m : List.of("gemini-3.5-flash-lite", "gemini-flash-latest", "gemini-3.6-flash", "gemini-2.5-flash-lite")) {
            if (!modelsToTry.contains(m)) {
                modelsToTry.add(m);
            }
        }

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.2
                )
        );

        String lastErrorMsg = null;
        for (String modelName : modelsToTry) {
            String url = String.format("%s/%s:generateContent", geminiApiUrl, modelName);
            try {
                String responseBody = restClient.post()
                        .uri(url)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestPayload)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode candidate = root.path("candidates").path(0);
                return candidate.path("content").path("parts").path(0).path("text").asText();
            } catch (RestClientResponseException e) {
                String errorBody = e.getResponseBodyAsString();
                logger.warn("Google Gemini API response for model '{}' (HTTP {}): {}", modelName, e.getStatusCode(), errorBody);

                if (e.getStatusCode().value() == 400 && errorBody.contains("API_KEY_INVALID")) {
                    throw new AiServiceUnavailableException("The configured Google Gemini API key is invalid. Please verify GEMINI_API_KEY in backend/.env.");
                } else if (e.getStatusCode().value() == 403) {
                    throw new AiServiceUnavailableException("Google Gemini API access denied. Please verify your API key permissions and Google AI Studio project status.");
                } else if (e.getStatusCode().value() == 429) {
                    throw new AiServiceUnavailableException("Google Gemini API rate limit or quota exceeded. Please try again in a few moments.");
                } else if (e.getStatusCode().value() == 404 || e.getStatusCode().value() == 503) {
                    lastErrorMsg = extractSafeErrorMessage(errorBody, e.getMessage());
                    // try fallback model
                    continue;
                } else {
                    lastErrorMsg = extractSafeErrorMessage(errorBody, e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("Error calling model '{}': {}", modelName, e.getMessage());
                lastErrorMsg = e.getMessage();
            }
        }

        throw new AiServiceUnavailableException("Google Gemini API was unable to generate classification: " + lastErrorMsg);
    }

    private String callOpenAi(String title, String description) {
        String apiKey = getEffectiveOpenaiKey();
        String prompt = buildPrompt(title, description);

        Map<String, Object> requestPayload = Map.of(
                "model", openaiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an AI customer support assistant. Output valid JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.2
        );

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(openaiApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.error("OpenAI API error (HTTP {}): {}", e.getStatusCode(), errorBody);

            if (e.getStatusCode().value() == 401) {
                throw new AiServiceUnavailableException("The configured OpenAI API key is invalid or unauthorized. Please verify OPENAI_API_KEY in backend/.env.");
            } else if (e.getStatusCode().value() == 429) {
                throw new AiServiceUnavailableException("OpenAI API rate limit or quota exceeded. Please check your OpenAI account billing.");
            } else {
                throw new AiServiceUnavailableException("OpenAI API returned HTTP " + e.getStatusCode().value() + ": " + extractSafeErrorMessage(errorBody, e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Failed to connect to OpenAI API: {}", e.getMessage(), e);
            throw new AiServiceUnavailableException("Failed to reach OpenAI service: " + e.getMessage(), e);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            logger.error("Failed to parse OpenAI API response: {}", responseBody, e);
            throw new AiServiceUnavailableException("Invalid response format received from OpenAI model", e);
        }
    }

    private String extractSafeErrorMessage(String responseBody, String defaultMsg) {
        if (!StringUtils.hasText(responseBody)) {
            return defaultMsg;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode err = root.path("error");
            if (err.has("message")) {
                return err.path("message").asText();
            }
        } catch (Exception ignored) {
        }
        return defaultMsg;
    }

    private String buildPrompt(String title, String description) {
        return """
                You are an intelligent support ticket classifier for SupportDesk.
                Analyze the ticket title and description provided below and return structured JSON with exactly these fields:
                - "category": One of ["TECHNICAL", "BILLING", "GENERAL", "FEATURE_REQUEST"]
                - "priority": One of ["LOW", "MEDIUM", "HIGH", "URGENT"]
                - "reason": A clear, concise 1-2 sentence explanation of why this category and priority were selected.

                Classification Rules:
                - Category TECHNICAL: software bugs, system errors, crashes, broken functionality, API issues, integration failures.
                - Category BILLING: payment issues, invoices, charges, refunds, credit card updates, subscription pricing.
                - Category GENERAL: general questions, documentation inquiries, how-to questions, basic account support.
                - Category FEATURE_REQUEST: suggestions for new features, requests for enhancements or design improvements.

                Priority Rules:
                - Priority URGENT: complete service outage, critical data loss, security vulnerabilities, business operations completely blocked.
                - Priority HIGH: major feature broken with no workaround, severe impact on critical workflows, billing overcharges.
                - Priority MEDIUM: standard issue, regular bug with a workaround, general non-blocking operational question.
                - Priority LOW: minor cosmetic glitch, typo, small suggestion, curiosity, general feedback.

                Ticket Title: %s
                Ticket Description: %s

                Respond with ONLY a JSON object formatted as:
                {
                  "category": "TECHNICAL",
                  "priority": "HIGH",
                  "reason": "Explanation text"
                }
                """.formatted(title, description);
    }

    AiClassifyTicketResponse parseAndSanitizeResponse(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            throw new AiServiceUnavailableException("AI service returned empty response");
        }

        String cleaned = rawJson.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            JsonNode jsonNode = objectMapper.readTree(cleaned);

            String categoryStr = jsonNode.path("category").asText("");
            String priorityStr = jsonNode.path("priority").asText("");
            String reason = jsonNode.path("reason").asText("");

            TicketCategory category = sanitizeCategory(categoryStr);
            TicketPriority priority = sanitizePriority(priorityStr);

            if (!StringUtils.hasText(reason)) {
                reason = String.format("Suggested %s category and %s priority based on ticket analysis.",
                        formatCategoryLabel(category), formatPriorityLabel(priority));
            }

            return AiClassifyTicketResponse.builder()
                    .category(category)
                    .priority(priority)
                    .reason(reason)
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to parse AI JSON response: '{}'. Error: {}", cleaned, e.getMessage());
            throw new AiServiceUnavailableException("Could not parse AI response into structured ticket suggestion", e);
        }
    }

    private TicketCategory sanitizeCategory(String raw) {
        if (raw == null) {
            return TicketCategory.GENERAL;
        }
        String normalized = raw.trim().toUpperCase().replace("-", "_").replace(" ", "_");

        try {
            return TicketCategory.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            if (normalized.contains("TECH") || normalized.contains("BUG") || normalized.contains("ERROR")) {
                return TicketCategory.TECHNICAL;
            } else if (normalized.contains("BILL") || normalized.contains("PAY") || normalized.contains("INVOICE")) {
                return TicketCategory.BILLING;
            } else if (normalized.contains("FEAT") || normalized.contains("REQUEST") || normalized.contains("ENHANCE")) {
                return TicketCategory.FEATURE_REQUEST;
            } else if (normalized.contains("ACCOUNT") || normalized.contains("PROFILE") || normalized.contains("LOGIN")) {
                return TicketCategory.ACCOUNT;
            }
            return TicketCategory.GENERAL;
        }
    }

    private TicketPriority sanitizePriority(String raw) {
        if (raw == null) {
            return TicketPriority.MEDIUM;
        }
        String normalized = raw.trim().toUpperCase().replace("-", "_").replace(" ", "_");

        try {
            return TicketPriority.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            if (normalized.contains("URGENT") || normalized.contains("CRITICAL") || normalized.contains("BLOCKER")) {
                return TicketPriority.URGENT;
            } else if (normalized.contains("HIGH")) {
                return TicketPriority.HIGH;
            } else if (normalized.contains("LOW") || normalized.contains("MINOR")) {
                return TicketPriority.LOW;
            }
            return TicketPriority.MEDIUM;
        }
    }

    private String formatCategoryLabel(TicketCategory category) {
        return switch (category) {
            case TECHNICAL -> "Technical";
            case BILLING -> "Billing";
            case GENERAL -> "General";
            case FEATURE_REQUEST -> "Feature Request";
            case ACCOUNT -> "Account";
        };
    }

    private String formatPriorityLabel(TicketPriority priority) {
        return switch (priority) {
            case LOW -> "Low";
            case MEDIUM -> "Medium";
            case HIGH -> "High";
            case URGENT -> "Urgent";
        };
    }
}
