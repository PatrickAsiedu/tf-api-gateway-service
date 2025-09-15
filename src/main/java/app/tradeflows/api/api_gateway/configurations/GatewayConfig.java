package app.tradeflows.api.api_gateway.configurations;

import app.tradeflows.api.api_gateway.filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r.path("/order-service/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://order-service"))
                .route("user-service", r -> r.path("/user-service/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://user-service"))
                .route("reporting-service", r -> r.path("/reporting-service/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://reporting-service"))
                .route("market-data-service", r -> r.path("/market-data-service/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://market-data-service"))
                .build();
    }
}