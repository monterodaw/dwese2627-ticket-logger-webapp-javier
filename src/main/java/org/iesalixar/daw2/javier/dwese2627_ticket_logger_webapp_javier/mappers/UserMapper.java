package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers;

import org.iesalixar.daw2.javimontero.crud_usuario.dtos.*;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.UserProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Mapper utilitario entre la entidad User y sus DTOs.
 * Implementación simple sin frameworks de mapeo.
 */
public class UserMapper {

    // ─────────────────────────────────────────
    // Entity → DTO (listado/tabla)
    // ─────────────────────────────────────────

    /** Convierte una entidad {@link User} a {@link UserDTO}. */
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.getEmailVerified());
        dto.setMustChangePassword(entity.getMustChangePassword());

        // ──────────────────────────────
        // Cargar roles si existen
        // ──────────────────────────────
        if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
            Set<String> roleNames = entity.getRoles().stream()
                    .map(Role::getName) // o Role::getDisplayName si lo prefieres
                    .collect(Collectors.toCollection(TreeSet::new)); // ordenados
            dto.setRoles(roleNames);
        } else {
            dto.setRoles(new HashSet<>());
        }
        return dto;
    }

    /** Convierte una lista de {@link User} a una lista de {@link UserDTO}. */
    public static List<UserDTO> toDTOList(List<User> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(UserMapper::toDTO).toList();
    }

    // ─────────────────────────────────────────
    // Entity → DTO (detalle con roles y perfil)
    // ─────────────────────────────────────────

    /**
     * Convierte una entidad {@link User} a {@link UserDetailDTO},
     * incluyendo sus roles y los datos de su perfil (si existe).
     */
    public static UserDetailDTO toDetailDTO(User entity) {
        if (entity == null) return null;

        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.getEmailVerified());
        dto.setMustChangePassword(entity.getMustChangePassword());

        // ──────────────────────────────
        // Roles
        // ──────────────────────────────
        if (entity.getRoles() != null) {
            Set<RoleDTO> roles = entity.getRoles().stream()
                    .map(RoleMapper::toDTO)
                    .collect(Collectors.toSet());
            dto.setRoles(roles);
        }

        // ──────────────────────────────
        // Cargar datos del perfil si existe
        // ──────────────────────────────
        UserProfile profile = entity.getProfile();
        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setProfileImage(profile.getProfileImage());
            dto.setBio(profile.getBio());
            dto.setLocale(profile.getLocale());
        }
        return dto;
    }

    /** Convierte un {@link User} a {@link UserUpdateDTO} para precargar el formulario de edición. */
    public static UserUpdateDTO toUpdateDTO(User entity) {
        if (entity == null) return null;

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.getEmailVerified());
        dto.setMustChangePassword(entity.getMustChangePassword());

        // UD04-5: ids de los roles para marcar las opciones del select múltiple
        if (entity.getRoles() != null) {
            dto.setRoleIds(entity.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    // ─────────────────────────────────────────
    // DTO (create/update) → Entity (con roles)
    // ─────────────────────────────────────────

    /**
     * Crea una nueva entidad {@link User} desde un {@link UserCreateDTO}
     * asignándole los roles ya cargados desde la base de datos.
     *
     * @param dto   datos del formulario.
     * @param roles roles a asignar (cargados por id desde el DAO).
     */
    public static User toEntity(UserCreateDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        User e = new User();
        e.setEmail(dto.getEmail());
        e.setPasswordHash(dto.getPasswordHash());
        e.setActive(Boolean.TRUE.equals(dto.getActive()));
        e.setAccountNonLocked(Boolean.TRUE.equals(dto.getAccountNonLocked()));
        e.setLastPasswordChange(dto.getLastPasswordChange());
        e.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        e.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        e.setEmailVerified(Boolean.TRUE.equals(dto.getEmailVerified()));
        e.setMustChangePassword(Boolean.TRUE.equals(dto.getMustChangePassword()));
        e.setRoles(roles != null ? new HashSet<>(roles) : new HashSet<>());
        return e;
    }

    /**
     * Copia campos editables desde un {@link UserUpdateDTO} a una entidad existente,
     * reemplazando sus roles. Se mantiene el perfil y el estado de persistencia JPA.
     */
    public static void copyToExistingEntity(UserUpdateDTO dto, User entity, Set<Role> roles) {
        if (dto == null || entity == null) return;

        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setActive(Boolean.TRUE.equals(dto.getActive()));
        entity.setAccountNonLocked(Boolean.TRUE.equals(dto.getAccountNonLocked()));
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        entity.setEmailVerified(Boolean.TRUE.equals(dto.getEmailVerified()));
        entity.setMustChangePassword(Boolean.TRUE.equals(dto.getMustChangePassword()));

        // Se modifica el Set existente: Hibernate calcula qué filas de user_roles insertar/borrar
        entity.getRoles().clear();
        if (roles != null) {
            entity.getRoles().addAll(roles);
        }
        // No tocar entity.setId(...) ni entity.setProfile(...)
    }
}
