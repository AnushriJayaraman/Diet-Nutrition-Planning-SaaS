package com.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthTokenService tokens;
    private final AccountRepository accounts;

    public ApiAuthorizationInterceptor(AuthTokenService tokens, AccountRepository accounts) {
        this.tokens = tokens;
        this.accounts = accounts;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.startsWith("/api/auth/") || "OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        AuthenticatedUser user = authorization != null && authorization.startsWith("Bearer ")
            ? tokens.validate(authorization.substring(7))
            : null;
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "A valid login is required");
            return false;
        }
        boolean activeAccount = accounts.findByEmail(user.email())
            .filter(account -> account.isEnabled() && account.getRole().equals(user.role()))
            .isPresent();
        if (!activeAccount) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "This account is disabled or no longer active");
            return false;
        }

        Set<String> allowedRoles = allowedRoles(path, request.getMethod());
        if (!allowedRoles.contains(user.role())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your account role cannot access this resource");
            return false;
        }
        request.setAttribute("authenticatedUser", user);
        return true;
    }

    private Set<String> allowedRoles(String path, String method) {
        if (path.equals("/api/patient-profile")) {
            if ("GET".equals(method)) {
                return Set.of("admin", "nutritionist", "patient");
            }
            return "POST".equals(method) ? Set.of("patient") : Set.of();
        }
        if (path.equals("/api/diet-plans") || path.matches("/api/diet-plans/\\d+")) {
            return "GET".equals(method)
                ? Set.of("nutritionist", "patient")
                : Set.of("nutritionist");
        }
        if (path.equals("/api/appointments")) {
            if ("GET".equals(method)) {
                return Set.of("admin", "nutritionist", "patient");
            }
            return "POST".equals(method) ? Set.of("patient") : Set.of();
        }
        if (path.equals("/api/appointment-options")) {
            return "GET".equals(method) ? Set.of("patient") : Set.of();
        }
        if (path.equals("/api/appointment-slots")) {
            return "GET".equals(method) ? Set.of("patient") : Set.of();
        }
        if (Set.of("/api/nutritionist/profile", "/api/nutritionist/availability").contains(path)) {
            return Set.of("GET", "PUT").contains(method) ? Set.of("nutritionist") : Set.of();
        }
        if (path.matches("/api/nutritionist/patients/[^/]+/details")) {
            return "GET".equals(method) ? Set.of("nutritionist") : Set.of();
        }
        if (path.matches("/api/appointments/\\d+")) {
            return "PATCH".equals(method) ? Set.of("nutritionist", "patient") : Set.of();
        }
        if (path.equals("/api/patient/dashboard")) {
            return Set.of("patient");
        }
        if (path.equals("/api/patient-assignments")) {
            return Set.of("admin");
        }
        if (path.equals("/api/nutrition-patients")) {
            return "GET".equals(method) ? Set.of("admin", "nutritionist") : Set.of();
        }
        if (path.startsWith("/api/admin/") || path.startsWith("/api/reports/")
            || path.equals("/api/nutritionists") || path.startsWith("/api/users") || path.equals("/api/patients")) {
            return Set.of("admin");
        }
        if (path.startsWith("/api/nutritionist/")) {
            return Set.of("admin", "nutritionist");
        }
        if (path.equals("/api/bmi")) {
            return "GET".equals(method) ? Set.of("nutritionist") : Set.of();
        }
        if (Set.of("/api/nutrition-tracking", "/api/nutrition-progress").contains(path)) {
            return "GET".equals(method) ? Set.of("nutritionist") : Set.of();
        }
        if (Set.of("/api/patient-bmi", "/api/patient-nutrition-tracking", "/api/patient-progress").contains(path)) {
            return "GET".equals(method) ? Set.of("nutritionist", "patient") : Set.of("patient");
        }
        return Set.of();
    }
}
