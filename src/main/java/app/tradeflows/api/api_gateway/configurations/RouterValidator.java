package app.tradeflows.api.api_gateway.configurations;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service
public class RouterValidator {

    public static final List<String> openEndpoints = List.of(
            "/auth/register","/auth/login","/auth/verify",
            "/auth/reset-password","/auth/forgot-password", "/swagger-ui",
            "/swagger-resources", "/eureka",
            "/webjars/swagger-ui/",
            "/v3/api-docs", "/actuator",
            "/market-data-service/"
    );


    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}