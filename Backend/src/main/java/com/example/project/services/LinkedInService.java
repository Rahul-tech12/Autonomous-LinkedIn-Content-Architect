package com.example.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LinkedInService {

    @Value("${linkedin.access.token}")
    private String defaultAccessToken;

    @Value("${linkedin.person.urn}")
    private String defaultPersonUrn;

    private static final String LINKEDIN_URL = "https://api.linkedin.com/v2/ugcPosts";

    private final RestTemplate restTemplate;

    public LinkedInService() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setConnectionRequestTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    // ✅ Original method — uses token from application.properties
    // Used by AgentService for your own account
    public void publishPost(String content) {
        publishPostForUser(content, defaultAccessToken, defaultPersonUrn);
    }

    // ✅ New method — accepts token and urn as parameters
    // Used by AgentService when posting for ANY user
    public void publishPostForUser(String content,
                                   String accessToken,
                                   String personUrn) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> shareCommentary = new HashMap<>();
            shareCommentary.put("text", content);

            Map<String, Object> shareContent = new HashMap<>();
            shareContent.put("shareCommentary", shareCommentary);
            shareContent.put("shareMediaCategory", "NONE");

            Map<String, Object> specificContent = new HashMap<>();
            specificContent.put("com.linkedin.ugc.ShareContent", shareContent);

            Map<String, Object> visibility = new HashMap<>();
            visibility.put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC");

            Map<String, Object> body = new HashMap<>();
            body.put("author", "urn:li:person:" + personUrn); // ✅ user's own URN
            body.put("lifecycleState", "PUBLISHED");
            body.put("specificContent", specificContent);
            body.put("visibility", visibility);

            // ✅ Serialize to bytes for exact Content-Length
            String jsonBody = mapper.writeValueAsString(body);
            byte[] jsonBytes = jsonBody.getBytes("UTF-8");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken); // ✅ user's own token
            headers.set("X-Restli-Protocol-Version", "2.0.0");
            headers.setContentLength(jsonBytes.length);

            HttpEntity<byte[]> entity = new HttpEntity<>(jsonBytes, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    LINKEDIN_URL, entity, String.class
            );

            log.info("✅ LinkedIn post published. Status: {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("LinkedIn API error: {}", e.getMessage());
            throw new RuntimeException("LinkedIn posting failed: " + e.getMessage());
        }
    }
}