package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.daos.RoleDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.daos.UserDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDetailDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserUpdateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación de la lógica de negocio (casos de uso) para el CRUD de {@link User}.
 * <p>
 * Esta capa se encarga de:
 * <ul>
 *     <li>Interactuar con los DAOs para acceder a base de datos.</li>
 *     <li>Aplicar reglas de negocio (email único, al menos un rol, caducidad de contraseña...).</li>
 *     <li>Transformar entidades a DTOs y viceversa mediante {@link UserMapper}.</li>
 *     <li>Lanzar excepciones semánticas ({@link ResourceNotFoundException},
 *     {@link DuplicateResourceException}) para que el controlador decida cómo mostrarlas.</li>
 * </ul>
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /** Número de meses de validez de una contraseña desde su último cambio. */
    private static final int PASSWORD_VALIDITY_MONTHS = 3;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Devuelve una página de usuarios ordenada.
     * Si se pide una página fuera de rango, se devuelve la última.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> list(int page, int size, String sortField, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        long totalElements = userDAO.countUsers();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }

        List<User> users = userDAO.listUsersPage(page, size, sortField, sortDir);
        // El mapeo se hace dentro de la transacción: así se pueden cargar los roles (LAZY)
        List<UserDTO> dtos = UserMapper.toDTOList(users);
        return new PageImpl<>(dtos, PageRequest.of(page, size), totalElements);
    }

    /**
     * Obtiene los datos necesarios para cargar el formulario de edición.
     *
     * @throws ResourceNotFoundException si no existe el usuario con ese id.
     */
    @Override
    @Transactional(readOnly = true)
    public UserUpdateDTO getForEdit(Long id) {
        User user = userDAO.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("user", "id", id);
        }
        return UserMapper.toUpdateDTO(user);
    }

    /**
     * Crea un nuevo usuario con sus roles.
     *
     * @throws DuplicateResourceException si ya existe un usuario con el mismo email.
     * @throws ResourceNotFoundException  si alguno de los roles seleccionados no existe.
     */
    @Override
    public void create(UserCreateDTO dto) {
        if (userDAO.existsUserByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("user", "email", dto.getEmail());
        }

        // >>> AUTOCÁLCULO de passwordExpiresAt (no viene de la vista)
        if (dto.getLastPasswordChange() == null) {
            dto.setLastPasswordChange(LocalDateTime.now());
        }
        dto.setPasswordExpiresAt(calculatePasswordExpiration(dto.getLastPasswordChange()));

        // Obtener de la base de datos los roles desde roleIds que es lo que llega de la vista
        Set<Role> roles = loadRoles(dto.getRoleIds());

        // Mapear DTO + entidad User incluyendo roles
        User user = UserMapper.toEntity(dto, roles);
        userDAO.insertUser(user);
        logger.info("Usuario {} insertado con éxito. Expira el {}", user.getEmail(), user.getPasswordExpiresAt());
    }

    /**
     * Actualiza un usuario existente y reemplaza sus roles.
     *
     * @throws DuplicateResourceException si el email ya lo está usando otro usuario.
     * @throws ResourceNotFoundException  si no existe el usuario o alguno de los roles.
     */
    @Override
    public void update(UserUpdateDTO dto) {
        if (userDAO.existsUserByEmailAndNotId(dto.getEmail(), dto.getId())) {
            throw new DuplicateResourceException("user", "email", dto.getEmail());
        }

        User user = userDAO.getUserById(dto.getId());
        if (user == null) {
            throw new ResourceNotFoundException("user", "id", dto.getId());
        }

        if (dto.getLastPasswordChange() == null) {
            dto.setLastPasswordChange(LocalDateTime.now());
        }
        dto.setPasswordExpiresAt(calculatePasswordExpiration(dto.getLastPasswordChange()));

        Set<Role> roles = loadRoles(dto.getRoleIds());
        UserMapper.copyToExistingEntity(dto, user, roles);
        userDAO.updateUser(user);
        logger.info("Usuario con id {} actualizado con éxito.", user.getId());
    }

    /**
     * Elimina un usuario (sus roles y su perfil se eliminan en cascada).
     * Si tenía imagen de perfil, también se borra del disco.
     *
     * @throws ResourceNotFoundException si no existe el usuario.
     */
    @Override
    public void delete(Long id) {
        User user = userDAO.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("user", "id", id);
        }
        String imagePath = user.getProfile() != null ? user.getProfile().getProfileImage() : null;

        userDAO.deleteUser(id);

        if (imagePath != null && !imagePath.isBlank()) {
            fileStorageService.deleteFile(imagePath);
        }
        logger.info("Usuario con id {} eliminado con éxito.", id);
    }

    /**
     * Devuelve el detalle de un usuario (con sus roles y su perfil).
     *
     * @throws ResourceNotFoundException si no existe el usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailDTO getDetail(Long id) {
        User user = userDAO.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("user", "id", id);
        }
        return UserMapper.toDetailDTO(user);
    }

    /**
     * Carga los roles a partir de sus ids y comprueba que existen todos.
     */
    private Set<Role> loadRoles(Set<Long> roleIds) {
        Set<Role> roles = new HashSet<>(roleDAO.findAllByIds(roleIds));
        if (roleIds != null && roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("role", "id", roleIds);
        }
        return roles;
    }

    /**
     * Calcula la fecha de caducidad de la contraseña: tres meses posterior al último cambio.
     */
    private LocalDateTime calculatePasswordExpiration(LocalDateTime lastPasswordChange) {
        return lastPasswordChange.plusMonths(PASSWORD_VALIDITY_MONTHS);
    }
}
