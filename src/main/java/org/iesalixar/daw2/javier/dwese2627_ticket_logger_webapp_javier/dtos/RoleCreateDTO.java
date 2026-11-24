package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el formulario de alta de roles.
 * El id va siempre a null (la vista lo usa para distinguir alta/edición).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleCreateDTO {

    private Long id;

    @NotBlank(message = "{msg.role.name.notEmpty}")
    @Size(max = 50, message = "{msg.role.name.size}")
    private String name;

    @NotBlank(message = "{msg.role.displayName.notEmpty}")
    @Size(max = 100, message = "{msg.role.displayName.size}")
    private String displayName;

    @Size(max = 255, message = "{msg.role.description.size}")
    private String description;
}
