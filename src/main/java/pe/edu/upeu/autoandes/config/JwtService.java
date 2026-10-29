package pe.edu.upeu.autoandes.config;

import java.time.*;
import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {
    private final SecretKey key;
    private final Clock clock;
    private final long expiraEnSegundos;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expira-en-segundos}") long expiraEnSegundos, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.clock = clock;
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public long expiraEnSegundos() {
        return expiraEnSegundos;
    }

    public String generar(String username) {
        Instant ahora = clock.instant();
        return Jwts.builder().issuer("autoandes").subject(username).issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plusSeconds(expiraEnSegundos))).signWith(key, Jwts.SIG.HS256).compact();
    }

    public String validarYObtenerUsuario(String token) {
        var jwt = Jwts.parser().verifyWith(key).requireIssuer("autoandes").clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token);
        if (!"HS256".equals(jwt.getHeader().getAlgorithm()) || jwt.getPayload().getExpiration() == null || jwt.getPayload().getSubject() == null || jwt.getPayload().getSubject().isBlank())
            throw new JwtException("Token inválido");
        return jwt.getPayload().getSubject();
    }
}
