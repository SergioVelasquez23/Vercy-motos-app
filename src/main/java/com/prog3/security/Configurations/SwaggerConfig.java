package com.prog3.security.Configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI para la documentación automática de la API del sistema "Vercy
 * Motos".
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(apiComponents())
                .security(List.of(new SecurityRequirement().addList("Bearer Authentication")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Vercy Motos - API REST")
                .description("""
                        API REST para el sistema de gestión "Vercy Motos".
                    
                    ## Características principales:
                    - **Gestión completa de pedidos** con validación de caja abierta
                    - **Control de inventario** automático
                    - **Sistema de pagos** múltiple (efectivo, transferencia, tarjeta)
                    - **Mesas especiales** con pedidos nombrados
                    - **Cuadre de caja** integrado
                    - **Reportes y dashboard** en tiempo real
                    - **WebSocket** para notificaciones
                    - **Cancelación selectiva** de ingredientes
                    
                    ## Tipos de pago soportados:
                    - **Pagado**: Pago normal con propina opcional
                    - **Cortesía**: Sin costo (cumpleaños, promociones, etc.)
                    - **Consumo interno**: Para empleados/gerencia
                    - **Cancelado**: Pedidos cancelados con motivo
                    
                    ## Estados de pedido:
                    - `pendiente`: Pedido creado, esperando preparación
                    - `enProceso`: Pedido en preparación
                    - `completado`: Pedido listo para entrega
                    - `pagado`: Pedido pagado y cerrado
                    - `cortesia`: Pedido gratuito
                    - `consumo_interno`: Para personal del restaurante
                    - `cancelado`: Pedido cancelado
                    
                    ## Autenticación:
                    Utiliza JWT Bearer token en el header Authorization.
                    """)
                .version("1.0.0")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Equipo de Desarrollo - Vercy Motos").email("desarrollo@vercymotos.com")
                .url("https://vercymotos.com");
    }

    private License apiLicense() {
        return new License()
                .name("Licencia Privada")
                .url("https://vercymotos.com/license");
    }

    private List<Server> apiServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Servidor de desarrollo local"),
            new Server()
                        .url("https://api.vercymotos.com")
                .description("Servidor de producción")
        );
    }

    private Components apiComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token para autenticación")
                );
    }
}
