package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para mostrar usuarios en el listado (tabla).
 * Incluye los nombres de sus roles.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Boolean active;
    private Boolean accountNonLocked;
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;
    private Integer failedLoginAttempts;
    private Boolean emailVerified;
    private Boolean mustChangePassword;
    /** Nombres de los roles (ROLE_ADMIN, ROLE_USER...) para pintarlos en la tabla. */
    private Set<String> roles = new HashSet<>();
}
