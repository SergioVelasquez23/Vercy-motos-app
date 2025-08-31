package com.prog3.security.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class RequestURL {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    public String twoFactorEmail(String email, String name) {
        try {
            Map<String, Object> bodyMap = new HashMap<>();
            Map<String, String> recipient = new HashMap<>();
            recipient.put("name", name);
            recipient.put("email", email);

            List<Map<String, String>> recipients = new ArrayList<>();
            recipients.add(recipient);

            bodyMap.put("recipients", recipients);

            String body = gson.toJson(bodyMap);

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/send2fa"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Extraer el código de la respuesta
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = gson.fromJson(response.body(), Map.class);
                return responseBody.get("code").toString();
            } else {
                throw new RuntimeException("Error al enviar el correo: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("Error al enviar el correo electrónico:");
            e.printStackTrace();
            throw new RuntimeException("Error al enviar el correo");
        }
    }

    public String sendEmail(String message, String recipient) {
        try {
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("message", message);
            bodyMap.put("recipient", recipient);

            String body = gson.toJson(bodyMap);

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/send_email"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return "The message has been sent";
            } else {
                throw new RuntimeException("Error sending email: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending email", e);
        }
    }

    public String sendResetLink(String message, String recipient) {
        try {
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("message", message);
            bodyMap.put("recipient", recipient);

            String body = gson.toJson(bodyMap);

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/send_reset_link"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return "The reset link has been sent";
            } else {
                throw new RuntimeException("Error sending reset link: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending reset link", e);
        }
    }

    public String sendPaymentNotification(String message, String recipient) {
        try {
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("message", message);
            bodyMap.put("recipient", recipient);

            String body = gson.toJson(bodyMap);

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/payment_notification"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return "The payment notification has been sent";
            } else {
                throw new RuntimeException("Error sending payment notification: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending payment notification", e);
        }
    }
}
