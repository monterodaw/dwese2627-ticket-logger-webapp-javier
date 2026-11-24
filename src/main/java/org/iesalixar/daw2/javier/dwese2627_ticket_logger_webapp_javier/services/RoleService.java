package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Casos de uso del CRUD de roles.
 */
public interface RoleService {

    Page<RoleDTO> list(int page, int size, String sortField, String sortDir);

    /** Todos los roles (para el select múltiple del formulario de usuario). */
    List<RoleDTO> listAll();

    RoleUpdateDTO getForEdit(Long id);

    void create(RoleCreateDTO dto);

    void update(RoleUpdateDTO dto);

    void delete(Long id);

    /** Número de usuarios que tienen asignado el rol (para no borrar roles en uso). */
    long countUsers(Long id);

    RoleDTO getDetail(Long id);
}
