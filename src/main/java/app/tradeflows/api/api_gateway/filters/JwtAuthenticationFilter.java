package app.tradeflows.api.api_gateway.filters;

import app.tradeflows.api.api_gateway.configurations.RouterValidator;
import app.tradeflows.api.api_gateway.entities.User;
import app.tradeflows.api.api_gateway.exceptions.AccountStatusException;
import app.tradeflows.api.api_gateway.repositories.UserRepository;
import app.tradeflows.api.api_gateway.services.JwtService;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter, Ordered {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private JwtService jwtService;
    private UserRepository userRepository;
    private RouterValidator validator;

    @Override
    public Mono<Void> filter( ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
                String token = authHeader.substring(7);

                try {

                    if (!jwtService.isTokenValid(token)) {
                        return Mono.error(new InvalidCredentialsException("Invalid JWT token"));
                    }

                    String username = jwtService.extractUsername(token);
                    Optional<User> userValid = userRepository.findByEmail(username);
                    if(userValid.isEmpty()){
                        return Mono.error(new InvalidCredentialsException("Invalid JWT token"));
                    }
                    User user = userValid.get();
                    if(!user.isActive()){
                        return Mono.error(new AccountStatusException("Account not active"));
                    }
                    // Create a new request with additional headers
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-AUTHENTICATED", "true")
                            .header("X-USER-ID", user.getId())
                            .header("X-USER-EMAIL", user.getEmail())
                            .header("X-USER-ROLE", String.valueOf(user.getRole()))
                            .header("X-CLIENT-IP", getClient(request))
                            .build();

                    // Mutate the exchange with the modified request
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();

                    return chain.filter(modifiedExchange);

                } catch (Exception e) {
                    // Invalid token, reject the request
                    logger.error(e.toString(), e);
                    return Mono.error(new SignatureException("Invalid JWT token"));
                }
            } else {
                // No token found, reject the request
                return Mono.error(new SignatureException("Bearer Authorization header missing"));
            }
        }
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-AUTHENTICATED", "false").build();

        // Mutate the exchange with the modified request
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        return chain.filter(modifiedExchange);
    }

    public String getClient(ServerHttpRequest request){
        String clientIp = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String serverIp = inetAddress.getHostAddress();
            String XforwardedIp = request.getHeaders().getFirst("X-Forwarded-For");
            logger.info("serverIp {}", serverIp);
            logger.info("getRemoteAddr() {}", request.getRemoteAddress());
            logger.info("XforwardedIp {}", XforwardedIp);
            if (!Objects.equals(clientIp, "127.0.0.1") && Objects.nonNull(XforwardedIp)) {
                return XforwardedIp;
            }
            return clientIp;
        }catch(Exception ex){
            return clientIp;
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
