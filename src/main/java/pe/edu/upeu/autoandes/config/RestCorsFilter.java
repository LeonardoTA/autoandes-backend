package pe.edu.upeu.autoandes.config;

import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;
import pe.edu.upeu.autoandes.security.RestAccessDeniedHandler;

/**
 * Keep rejected CORS requests on the same JSON error contract as authorization.
 */
public class RestCorsFilter extends CorsFilter {
    public RestCorsFilter(CorsConfigurationSource source, RestAccessDeniedHandler denied) {
        super(source);
        var delegate = new DefaultCorsProcessor();
        setCorsProcessor((configuration, request, response) -> {
            if (CorsUtils.isCorsRequest(request)) {
                boolean preflight = CorsUtils.isPreFlightRequest(request);
                String method = preflight ? request.getHeader("Access-Control-Request-Method") : request.getMethod();
                String headers = request.getHeader("Access-Control-Request-Headers");
                boolean allowed = configuration != null
                        && configuration.checkOrigin(request.getHeader("Origin")) != null
                        && configuration.checkHttpMethod(HttpMethod.valueOf(method)) != null
                        && (!preflight || headers == null || configuration.checkHeaders(Arrays.stream(headers.split(",")).map(String::trim).toList()) != null);
                if (!allowed) {
                    denied.handle(request, response, new AccessDeniedException("Solicitud CORS no permitida"));
                    return false;
                }
            }
            return delegate.processRequest(configuration, request, response);
        });
    }
}
