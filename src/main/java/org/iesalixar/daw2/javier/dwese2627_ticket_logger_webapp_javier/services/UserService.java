package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDetailDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserUpdateDTO;
import org.springframework.data.domain.Page;

/**
 * Casos de uso del CRUD de usuarios.
 */
public interface UserService {

    Page<UserDTO> list(int page, int size, String sortField, String sortDir);

    UserUpdateDTO getForEdit(Long id);

    void create(UserCreateDTO dto);

    void update(UserUpdateDTO dto);

    void delete(Long id);

    UserDetailDTO getDetail(Long id);
}
