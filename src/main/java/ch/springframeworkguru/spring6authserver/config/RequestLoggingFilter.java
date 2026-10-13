package ch.springframeworkguru.spring6authserver.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.List;

@RequiredArgsConstructor
public class RequestLoggingFilter extends CommonsRequestLoggingFilter {

    private final List<String> excludePaths;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return excludePaths.stream().anyMatch(requestUri::startsWith);
    }

}
