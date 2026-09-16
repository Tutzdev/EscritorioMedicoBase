package io.github.officemed.medical_office_api.auth.security;

import io.github.officemed.medical_office_api.auth.exception.InvalidRefreshTokenException;
import io.github.officemed.medical_office_api.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    private final String issuer;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.access-token-expiration}") Duration accessTokenExpiration,
            @Value("${app.security.refresh-token-expiration}") Duration refreshTokenExpiration,
            @Value("${app.security.issuer}") String issuer
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must contain at least 32 characters"
            );
        }

        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    public String createAccessToken(User user) {
        return createToken(user, ACCESS_TOKEN_TYPE, accessTokenExpiration);
    }

    public String createRefreshToken(User user) {
        return createToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
    }

    public UUID extractAccessTokenUserId(String token) {
        return extractUserId(token, ACCESS_TOKEN_TYPE);
    }

    public UUID extractRefreshTokenUserId(String token) {
        try {
            return extractUserId(token, REFRESH_TOKEN_TYPE);
        } catch (RuntimeException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration.toSeconds();
    }

    private String createToken(
            User user,
            String tokenType,
            Duration expiration
    ) {
        Instant issuedAt = Instant.now();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("role", user.getRole().name());
        claims.put("type", tokenType);
        claims.put("iss", issuer);
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", issuedAt.plus(expiration).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedClaims = encodeJson(claims);
        String unsignedToken = encodedHeader + "." + encodedClaims;

        return unsignedToken + "." + sign(unsignedToken);
    }

    private UUID extractUserId(
            String token,
            String expectedTokenType
    ) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed token");
        }

        byte[] expectedSignature = decode(sign(parts[0] + "." + parts[1]));
        byte[] receivedSignature = decode(parts[2]);

        if (!MessageDigest.isEqual(expectedSignature, receivedSignature)) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        Map<String, Object> claims = decodeClaims(parts[1]);
        String tokenType = String.valueOf(claims.get("type"));
        String tokenIssuer = String.valueOf(claims.get("iss"));
        long expiresAt = ((Number) claims.get("exp")).longValue();

        if (!expectedTokenType.equals(tokenType)
                || !issuer.equals(tokenIssuer)
                || Instant.now().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        return UUID.fromString(String.valueOf(claims.get("sub")));
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create authentication token", exception);
        }
    }

    private Map<String, Object> decodeClaims(String encodedClaims) {
        try {
            return objectMapper.readValue(
                    decode(encodedClaims),
                    new TypeReference<>() {
                    }
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token claims", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign authentication token", exception);
        }
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
