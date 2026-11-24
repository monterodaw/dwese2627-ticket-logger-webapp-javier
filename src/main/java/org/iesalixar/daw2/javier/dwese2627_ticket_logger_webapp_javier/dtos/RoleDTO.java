package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para mostrar roles (listado, detalle y opciones del select de usuarios).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String displayName;
    private String description;
}
