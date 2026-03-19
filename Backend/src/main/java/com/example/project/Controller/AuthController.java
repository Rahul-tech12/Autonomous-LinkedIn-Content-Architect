package com.example.project.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${linkedin.client.id}")
    private String clientId;

    @Value("${linkedin.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) {

        // ✅ Show LinkedIn error if it sent one
        if (error != null) {
            log.error("LinkedIn error: {} — {}", error, errorDescription);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", error,
                    "description", errorDescription
            ));
        }

        if (code == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No code received from LinkedIn"
            ));
        }

        log.info("Received code: {}", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type",    "authorization_code");
        body.add("code",          code);
        body.add("client_id",     clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri",  "http://localhost:8081/callback");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://www.linkedin.com/oauth/v2/accessToken",
                    request,
                    Map.class
            );

            String accessToken = (String) response.getBody().get("access_token");
            Integer expiresIn  = (Integer) response.getBody().get("expires_in");

            log.info("✅ Token received! Expires in {} seconds", expiresIn);
            log.info("📋 ACCESS TOKEN: {}", accessToken);

            return ResponseEntity.ok(Map.of(
                    "access_token",    accessToken,
                    "expires_in_days", String.valueOf(expiresIn / 86400),
                    "message",         "Copy access_token into application.properties"
            ));

        } catch (Exception e) {
            log.error("Token exchange failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error",   "Token exchange failed",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMe(
            @RequestParam("token") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            // ✅ New endpoint — replaces deprecated /v2/me
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.linkedin.com/v2/userinfo",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            String sub = (String) response.getBody().get("sub"); // this is your URN
            String name = (String) response.getBody().get("name");

            log.info("✅ Person URN (sub): {}", sub);

            return ResponseEntity.ok(Map.of(
                    "person_urn", sub,
                    "name", name,
                    "message", "Copy person_urn into application.properties as linkedin.person.urn"
            ));

        } catch (Exception e) {
            log.error("Failed to get profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
