package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para el formulario de alta de usuarios.
 * <p>
 * El campo {@code id} siempre va a null, pero se mantiene para que la vista
 * pueda distinguir entre alta y edición ({@code user.id == null}).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDTO {

    private Long id;

    @NotBlank(message = "{msg.user.email.notEmpty}")
    @Email(message = "{msg.user.email.invalid}")
    @Size(max = 100, message = "{msg.user.email.size}")
    private String email;

    @NotBlank(message = "{msg.user.passwordHash.notEmpty}")
    @Size(max = 500, message = "{msg.user.passwordHash.size}")
    private String passwordHash;

    private Boolean active = Boolean.TRUE;

    private Boolean accountNonLocked = Boolean.TRUE;

    @NotNull(message = "{msg.user.lastPasswordChange.notEmpty}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastPasswordChange;

    /** Solo lectura: se calcula en el servicio (lastPasswordChange + 3 meses). */
    private LocalDateTime passwordExpiresAt;

    @NotNull(message = "{msg.user.failedLoginAttempts.notNull}")
    @Min(value = 0, message = "{msg.user.failedLoginAttempts.min}")
    private Integer failedLoginAttempts = 0;

    private Boolean emailVerified = Boolean.FALSE;

    private Boolean mustChangePassword = Boolean.FALSE;

    /** UD04-5: ids de los roles seleccionados en el select múltiple. */
    @NotEmpty(message = "{msg.user.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();
}
