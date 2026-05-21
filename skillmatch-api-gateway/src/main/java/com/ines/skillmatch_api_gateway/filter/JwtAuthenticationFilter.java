package com.ines.skillmatch_api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // LAISSER PASSER LES REQUÊTES OPTIONS (CORS)
            if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getURI().getPath();
            // =============================================================
            // 1. AJOUTE CETTE CONDITION POUR IGNORER LE FILTRE SUR LE LOGIN
            // =============================================================
            if (path.contains("/api/auth/login") || path.contains("/api/auth/register") || path.contains("/oauth2/")) {
                return chain.filter(exchange);
            }

            // 2. Vérifier si le header Authorization existe (Reste du code identique)
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders()
                    .get(HttpHeaders.AUTHORIZATION).get(0);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                // Valider le token
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Ajouter les informations utilisateur aux headers pour les microservices suivants
                exchange.getRequest().mutate()
                        .header("X-User-Id", claims.get("userId", Object.class).toString())
                        .header("X-User-Role", claims.get("role", String.class))
                        .header("X-User-Email", claims.getSubject());

                return chain.filter(exchange);

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
        // Configuration properties if needed
    }
}
