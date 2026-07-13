package com.ines.frontend_skillmatch.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Service
public class SessionService {

    private static final String TOKEN_KEY = "user_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_ROLE_KEY = "user_role";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_INITIALS_KEY = "user_initials";
    private static final String IS_AUTHENTICATED_KEY = "is_authenticated";

    // Retrieves the current HTTP session, creating one if it doesn't exist.
    private HttpSession getSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest().getSession(true);
    }

    // Creates a new session after successful login, storing user info and JWT token.
    public void createSession(String token, Long userId, String email, String role) {
        HttpSession session = getSession();
        session.setAttribute(TOKEN_KEY, token);
        session.setAttribute(USER_ID_KEY, userId);
        session.setAttribute(USER_EMAIL_KEY, email);
        session.setAttribute(USER_ROLE_KEY, role);
        session.setAttribute(IS_AUTHENTICATED_KEY, true);

        // Generate initials from email (first letter)
        String initials = email != null ? email.substring(0, 1).toUpperCase() : "?";
        session.setAttribute(USER_INITIALS_KEY, initials);

        // Session timeout: 24 hours
        session.setMaxInactiveInterval(86400);
    }

    // Checks if the user is currently authenticated (session contains valid auth flag).
    public boolean isAuthenticated() {
        try {
            HttpSession session = getSession();
            Boolean auth = (Boolean) session.getAttribute(IS_AUTHENTICATED_KEY);
            return auth != null && auth;
        } catch (Exception e) {
            return false;
        }
    }

    // Creates an OAuth2 session (Google/GitHub) reusing the standard session creation.
    public void createOAuth2Session(String token, String email, String role, Long userId) {
        createSession(token, userId, email, role);
    }

    // Retrieves the JWT token stored in the session.
    public String getToken() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(TOKEN_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    // Retrieves the logged-in user's ID from session.
    public Long getUserId() {
        try {
            HttpSession session = getSession();
            return (Long) session.getAttribute(USER_ID_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    // Retrieves the logged-in user's email from session.
    public String getUserEmail() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_EMAIL_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    // Retrieves the logged-in user's role from session.
    public String getUserRole() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_ROLE_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    // Retrieves the user's initials from session (first letter of email or name).
    public String getUserInitials() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_INITIALS_KEY);
        } catch (Exception e) {
            return "?";
        }
    }

    // Returns a map containing all session information (token, userId, email, role, initials, auth status).
    public Map<String, Object> getSessionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("token", getToken());
        info.put("userId", getUserId());
        info.put("email", getUserEmail());
        info.put("role", getUserRole());
        info.put("initials", getUserInitials());
        info.put("isAuthenticated", isAuthenticated());
        return info;
    }

    // Checks if the user has a specific role (case-insensitive).
    public boolean hasRole(String role) {
        String userRole = getUserRole();
        return userRole != null && userRole.equalsIgnoreCase(role);
    }

    // Checks if the user is an ADMIN.
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    // Checks if the user is a CANDIDAT.
    public boolean isCandidat() {
        return hasRole("CANDIDAT");
    }

    // Checks if the user is an ENTREPRISE.
    public boolean isEntreprise() {
        return hasRole("ENTREPRISE");
    }

    // Updates the user's name in the session and regenerates initials.
    public void updateUserName(String name) {
        try {
            HttpSession session = getSession();
            session.setAttribute(USER_NAME_KEY, name);
            if (name != null && !name.isEmpty()) {
                session.setAttribute(USER_INITIALS_KEY, name.substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    // Refreshes the stored JWT token in the session (e.g., after renewal).
    public void refreshToken(String newToken) {
        try {
            HttpSession session = getSession();
            session.setAttribute(TOKEN_KEY, newToken);
        } catch (Exception e) {
            // Ignore
        }
    }

    // Invalidates the current session (logout).
    public void destroySession() {
        try {
            HttpSession session = getSession();
            session.invalidate();
        } catch (Exception e) {
            // Ignore
        }
    }

    // Returns the redirect URL (path) based on the user's role (e.g., "/dashboard-entreprise").
    public String getRedirectUrlByRole() {
        String role = getUserRole();
        if (role == null) return "/login";

        return switch (role.toUpperCase()) {
            case "ADMIN" -> "/admin";
            case "ENTREPRISE" -> "/dashboard-entreprise";
            case "CANDIDAT" -> "/dashboard-candidat";
            default -> "/login";
        };
    }

    // Returns the name of the HTML template (without .html) to display for the user's role.
    public String getDashboardPage() {
        String role = getUserRole();
        if (role == null) return "login";

        return switch (role.toUpperCase()) {
            case "ADMIN" -> "dashboard-admin";
            case "ENTREPRISE" -> "dashboard-entreprise";
            case "CANDIDAT" -> "dashboard-candidat";
            default -> "login";
        };
    }

    // Checks if the session has expired (token missing).
    public boolean isSessionExpired() {
        try {
            HttpSession session = getSession();
            return session.getAttribute(TOKEN_KEY) == null;
        } catch (Exception e) {
            return true;
        }
    }
}