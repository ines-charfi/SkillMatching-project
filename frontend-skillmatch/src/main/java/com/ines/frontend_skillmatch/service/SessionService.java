package com.ines.frontend_skillmatch.service;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion de session utilisateur côté frontend.
 * Stocke les informations de l'utilisateur connecté dans la session HTTP.
 */
@Service
public class SessionService {

    private static final String TOKEN_KEY = "user_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_ROLE_KEY = "user_role";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_INITIALS_KEY = "user_initials";
    private static final String IS_AUTHENTICATED_KEY = "is_authenticated";

    /**
     * Récupère la session HTTP courante
     */
    private HttpSession getSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest().getSession(true);
    }

    /**
     * Créer une session après connexion réussie
     */
    public void createSession(String token, Long userId, String email, String role) {
        HttpSession session = getSession();
        session.setAttribute(TOKEN_KEY, token);
        session.setAttribute(USER_ID_KEY, userId);
        session.setAttribute(USER_EMAIL_KEY, email);
        session.setAttribute(USER_ROLE_KEY, role);
        session.setAttribute(IS_AUTHENTICATED_KEY, true);

        // Générer les initiales
        String initials = email != null ? email.substring(0, 1).toUpperCase() : "?";
        session.setAttribute(USER_INITIALS_KEY, initials);

        // Durée de session : 24 heures
        session.setMaxInactiveInterval(86400);
    }


    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public boolean isAuthenticated() {
        try {
            HttpSession session = getSession();
            Boolean auth = (Boolean) session.getAttribute(IS_AUTHENTICATED_KEY);
            return auth != null && auth;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Créer une session OAuth2 après connexion Google/GitHub
     */
    public void createOAuth2Session(String token, String email, String role, Long userId) {
        createSession(token, userId, email, role);
    }


    /**
     * Récupère le token JWT stocké en session
     */
    public String getToken() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(TOKEN_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public Long getUserId() {
        try {
            HttpSession session = getSession();
            return (Long) session.getAttribute(USER_ID_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     */
    public String getUserEmail() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_EMAIL_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère le rôle de l'utilisateur connecté
     */
    public String getUserRole() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_ROLE_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère les initiales de l'utilisateur
     */
    public String getUserInitials() {
        try {
            HttpSession session = getSession();
            return (String) session.getAttribute(USER_INITIALS_KEY);
        } catch (Exception e) {
            return "?";
        }
    }

    /**
     * Récupère toutes les informations de session sous forme de Map
     */
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

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(String role) {
        String userRole = getUserRole();
        return userRole != null && userRole.equalsIgnoreCase(role);
    }

    /**
     * Vérifie si l'utilisateur est un ADMIN
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Vérifie si l'utilisateur est un CANDIDAT
     */
    public boolean isCandidat() {
        return hasRole("CANDIDAT");
    }

    /**
     * Vérifie si l'utilisateur est une ENTREPRISE
     */
    public boolean isEntreprise() {
        return hasRole("ENTREPRISE");
    }

    /**
     * Met à jour le nom d'utilisateur dans la session
     */
    public void updateUserName(String name) {
        try {
            HttpSession session = getSession();
            session.setAttribute(USER_NAME_KEY, name);
            if (name != null && !name.isEmpty()) {
                session.setAttribute(USER_INITIALS_KEY, name.substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {
            // Ignorer
        }
    }

    /**
     * Rafraîchit le token JWT
     */
    public void refreshToken(String newToken) {
        try {
            HttpSession session = getSession();
            session.setAttribute(TOKEN_KEY, newToken);
        } catch (Exception e) {
            // Ignorer
        }
    }

    /**
     * Détruit la session (déconnexion)
     */
    public void destroySession() {
        try {
            HttpSession session = getSession();
            session.invalidate();
        } catch (Exception e) {
            // Ignorer
        }
    }

    /**
     * Récupère l'URL de redirection (le chemin qui s'affiche dans le navigateur)
     * Doit commencer par un "/"
     */
    public String getRedirectUrlByRole() {
        String role = getUserRole();
        if (role == null) return "/login";

        return switch (role.toUpperCase()) {
            case "ADMIN" -> "/admin"; // URL pour l'admin
            case "ENTREPRISE" -> "/dashboard-entreprise"; // URL pour l'entreprise
            case "CANDIDAT" -> "/dashboard-candidat"; // URL pour le candidat
            default -> "/login";
        };
    }

    /**
     * Récupère le NOM DU FICHIER HTML (sans le .html)
     * Ne doit PAS commencer par un "/"
     */
    public String getDashboardPage() {
        String role = getUserRole();
        if (role == null) return "login";

        return switch (role.toUpperCase()) {
            case "ADMIN" -> "dashboard-admin";
            case "ENTREPRISE" -> "dashboard-entreprise"; // Le nom de ton fichier HTML
            case "CANDIDAT" -> "dashboard-candidat";   // Le nom de ton fichier HTML
            default -> "login";
        };
    }
    /**
     * Vérifie si la session est expirée
     */
    public boolean isSessionExpired() {
        try {
            HttpSession session = getSession();
            return session.getAttribute(TOKEN_KEY) == null;
        } catch (Exception e) {
            return true;
        }
    }
}