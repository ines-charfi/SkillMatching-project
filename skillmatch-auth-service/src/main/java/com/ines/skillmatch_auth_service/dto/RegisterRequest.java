package com.ines.skillmatch_auth_service.dto;
import com.ines.skillmatch_auth_service.model.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private User.Role role;

    // Champs optionnels selon le rôle
    private String nom;
    private String prenom;
    private String nomEntreprise;
}
