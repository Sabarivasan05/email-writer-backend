package com.email.writer.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.email.writer.Model.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService {

//    public EmailGeneratorService() {}

    @Value("${gemini.api.url}")
    private String gemini_api_url;

    @Value("${gemini.api.key}")
    private String gemini_api_key;

    private final WebClient webClient;


    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        //create a prompt
        String prompt = buildPrompt(emailRequest);

        //make a request
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[] {
                                Map.of("text", prompt)
                        })
                }
        );

        //send request to gemini and get respone
        String response = webClient.post()
                .uri(gemini_api_url + gemini_api_key)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //extract and return the response
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);
            return node.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }
        catch(Exception e) {
            return "Error in processing the request" + e.getMessage();
        }
    }

    public String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate an email reply content for the below mentioned passage. Don't include the subject line please. ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.").append(" please don't give subject.");
        }
        prompt.append("\n Original email content.\n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }

}
