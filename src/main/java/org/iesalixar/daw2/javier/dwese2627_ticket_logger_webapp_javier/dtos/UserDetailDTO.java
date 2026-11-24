package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleDTO;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para la vista de detalle de un usuario.
 * Incluye sus roles (UD04-5) y los datos de su perfil (UD04-4).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDTO {
    private Long id;
    private String email;
    private String passwordHash;
    private Boolean active;
    private Boolean accountNonLocked;
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;
    private Integer failedLoginAttempts;
    private Boolean emailVerified;
    private Boolean mustChangePassword;

    // ── Roles (N:M) ──
    private Set<RoleDTO> roles = new HashSet<>();

    // ── Perfil (1:1) ──
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImage;
    private String bio;
    private String locale;
}
