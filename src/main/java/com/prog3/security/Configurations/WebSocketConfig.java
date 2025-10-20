package com.prog3.security.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un message broker simple para enviar mensajes a los clientes
        config.enableSimpleBroker("/topic", "/queue");
        // Define el prefijo para los destinos de mensajes de la aplicación
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint WebSocket que los clientes usarán para conectarse
        // Endpoint con SockJS para compatibilidad
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permitir todas las conexiones para desarrollo
                .withSockJS(); // Habilita SockJS para compatibilidad con navegadores que no soportan WebSocket
        
        // Endpoint que el frontend espera: /ws/updates
        registry.addEndpoint("/ws/updates")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Endpoint WebSocket nativo para clientes que soportan WebSocket directamente
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*"); // Sin SockJS, WebSocket puro
    }
}
