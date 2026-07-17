package com.ines.skillmatch_auth_service.security;
import com.ines.skillmatch_auth_service.model.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
/**
 * Custom implementation of Spring Security's {@link UserDetails} and {@link OAuth2User}.
 *
 * This class wraps a {@link User} entity and provides the necessary information
 * for authentication and authorization, including:
 * - User credentials (email, password)
 * - Granted authorities (roles)
 * - Account status flags (enabled, expired, locked, etc.)
 * - OAuth2 attributes (for social login)
 *
 * It is used by Spring Security during authentication and by the OAuth2
 * authentication flow to represent the authenticated principal.
 */
@Data
@Getter
public class UserDetailsImpl implements UserDetails, OAuth2User {

    private Long id;
    private String email;
    private String password;
    private User.Role role;
    private boolean enabled;
    /**
     * Additional attributes from the OAuth2 provider (e.g., name, avatar, etc.).
     * Only populated when the user authenticates via OAuth2.
     */
    private Map<String, Object> attributes;
    /**
     * The list of granted authorities (roles) for the user.
     * Populated from the user's role, prefixed with "ROLE_".
     */
    private List<GrantedAuthority> authorities;
    /**
     * Constructs a UserDetailsImpl from a {@link User} entity.
     *
     * The authority list is built from the user's role, e.g., if role is CANDIDAT,
     * the authority becomes "ROLE_CANDIDAT".
     *
     * @param user the User entity from the database
     */
    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.enabled = user.getEnabled();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    /**
     * Factory method to build a UserDetailsImpl from a User entity.
     *
     * @param user the User entity
     * @return a new UserDetailsImpl instance
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(user);
    }
    /**
     * Factory method to build a UserDetailsImpl from a User entity and OAuth2 attributes.
     *
     * This is used when the user logs in via OAuth2 (e.g., Google). The attributes
     * map is stored for later use (e.g., to retrieve the user's name or avatar).
     *
     * @param user       the User entity
     * @param attributes the OAuth2 attributes from the provider
     * @return a new UserDetailsImpl instance with the attributes set
     */
    public static UserDetailsImpl build(User user, Map<String, Object> attributes) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        userDetails.setAttributes(attributes);
        return userDetails;
    }
    /**
     * Sets the OAuth2 attributes for this user.
     *
     * @param attributes the map of attributes from the OAuth2 provider
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    // ================================
    // OAuth2User interface methods
    // ================================

    /**
     * Returns the OAuth2 attributes for this user.
     *
     * @return the attributes map (may be null if not an OAuth2 login)
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
