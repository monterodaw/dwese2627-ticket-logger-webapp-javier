package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.*;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Province;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Region;

import java.util.List;

/**
 * Mapper utilitario entre entidades (Region/Province) y sus DTOs.
 * Implementación simple sin frameworks de mapeo.
 */
public class RegionMapper {

    // -------------------------------------------------------------
    // Entity -> DTO (Listado/Tabla Básica)
    // -------------------------------------------------------------

    /**
     * Convierte una entidad {@link Region} a {@link RegionDTO}.
     */
    public static RegionDTO toDTO(Region entity) {
        if (entity == null) return null;
        RegionDTO dto = new RegionDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }

    /**
     * Convierte una lista de entidades {@link Region} a {@link RegionDTO}.
     */
    public static List<RegionDTO> toDTOList(List<Region> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(RegionMapper::toDTO).toList();
    }

    // -------------------------------------------------------------
    // Entity -> DTO (Detalle con provincias)
    // -------------------------------------------------------------

    /**
     * Convierte una entidad {@link Region} a {@link RegionDetailDTO}, incluyendo su lista de provincias.
     */
    public static RegionDetailDTO toDetailDTO(Region entity) {
        if (entity == null) return null;
        RegionDetailDTO dto = new RegionDetailDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setProvinces(toProvinceDTOList(entity.getProvinces()));
        return dto;
    }

    /**
     * Convierte una entidad {@link Province} a {@link ProvinceDTO} incluyendo el nombre de la región.
     */
    public static ProvinceDTO toProvinceDTO(Province p) {
        if (p == null) return null;
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        if (p.getRegion() != null) {
            dto.setRegionName(p.getRegion().getName());
        }
        return dto;
    }

    /**
     * Convierte una lista de {@link Province} a una lista de {@link ProvinceDTO}.
     */
    public static List<ProvinceDTO> toProvinceDTOList(List<Province> provinces) {
        if (provinces == null) return List.of();
        return provinces.stream().map(RegionMapper::toProvinceDTO).toList();
    }

    /**
     * Convierte una entidad {@link Region} a {@link RegionUpdateDTO}.
     */
    public static RegionUpdateDTO toUpdateDTO(Region entity) {
        if (entity == null) return null;
        RegionUpdateDTO dto = new RegionUpdateDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }
    // -------------------------------------------------------------
    // DTO (create/update) -> Entity
    // -------------------------------------------------------------

    /**
     * Crea una nueva entidad {@link Region} desde un {@link RegionCreateDTO}.
     * (id se deja null para autogenerarse)
     */
    public static Region toEntity(RegionCreateDTO dto) {
        if (dto == null) return null;
        Region e = new Region();
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        return e;
    }

    /**
     * Crea una entidad {@link Region} desde un {@link RegionUpdateDTO}.
     * Útil si trabajas con update por reemplazo.
     * Si prefieres evitar perder relaciones, carga la entidad de BD y copia campos a mano.
     */
    public Region toEntity(RegionUpdateDTO dto) {
        if (dto == null) return null;
        Region e = new Region();
        e.setId(dto.getId());
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        return e;
    }

    /**
     * Copia campos editables de {@link RegionUpdateDTO} sobre una entidad existente.
     * Recomendado cuando quieres mantener relaciones y estado de persistencia.
     */
    public static void copyToExistingEntity(RegionUpdateDTO dto, Region entity) {
        if (dto == null || entity == null) return;
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        // No tocar entity.setId(...)
        // Tampoco tocar relaciones como entity.getProvinces() aquí.
    }

}
