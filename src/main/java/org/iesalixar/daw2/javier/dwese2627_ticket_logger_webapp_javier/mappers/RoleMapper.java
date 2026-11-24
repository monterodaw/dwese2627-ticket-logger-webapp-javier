package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers;

import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleUpdateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;

import java.util.List;

/**
 * Mapper utilitario entre la entidad Role y sus DTOs.
 * Implementación simple sin frameworks de mapeo.
 */
public class RoleMapper {

    // ─────────────────────────────────────────
    // Entity → DTO
    // ─────────────────────────────────────────

    /** Convierte una entidad {@link Role} a {@link RoleDTO}. */
    public static RoleDTO toDTO(Role entity) {
        if (entity == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    /** Convierte una lista de {@link Role} a una lista de {@link RoleDTO}. */
    public static List<RoleDTO> toDTOList(List<Role> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(RoleMapper::toDTO).toList();
    }

    /** Convierte un {@link Role} a {@link RoleUpdateDTO} para precargar el formulario de edición. */
    public static RoleUpdateDTO toUpdateDTO(Role entity) {
        if (entity == null) return null;
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    // ─────────────────────────────────────────
    // DTO (create/update) → Entity
    // ─────────────────────────────────────────

    /** Crea una nueva entidad {@link Role} desde un {@link RoleCreateDTO} (id null para autogenerarse). */
    public static Role toEntity(RoleCreateDTO dto) {
        if (dto == null) return null;
        Role e = new Role();
        e.setName(dto.getName());
        e.setDisplayName(dto.getDisplayName());
        e.setDescription(dto.getDescription());
        return e;
    }

    /**
     * Copia campos editables desde un {@link RoleUpdateDTO} a una entidad existente.
     * Así se mantienen las relaciones (usuarios) y el estado de persistencia JPA.
     */
    public static void copyToExistingEntity(RoleUpdateDTO dto, Role entity) {
        if (dto == null || entity == null) return;
        entity.setName(dto.getName());
        entity.setDisplayName(dto.getDisplayName());
        entity.setDescription(dto.getDescription());
        // No tocar entity.setId(...)
    }
}
