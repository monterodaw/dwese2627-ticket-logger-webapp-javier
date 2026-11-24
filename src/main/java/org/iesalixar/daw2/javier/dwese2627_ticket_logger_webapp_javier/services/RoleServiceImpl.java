package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.daos.RoleDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleUpdateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.mappers.RoleMapper;
import org.iesalixar.daw2.javimontero.crud_usuario.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de la lógica de negocio (casos de uso) para el CRUD de {@link Role}.
 * <p>
 * Reglas de negocio:
 * <ul>
 *     <li>El nombre técnico del rol es único.</li>
 * </ul>
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleDAO roleDAO;

    /** Devuelve una página de roles ordenada (si la página está fuera de rango se devuelve la última). */
    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> list(int page, int size, String sortField, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        long totalElements = roleDAO.countRoles();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }

        List<RoleDTO> dtos = RoleMapper.toDTOList(roleDAO.listRolesPage(page, size, sortField, sortDir));
        return new PageImpl<>(dtos, PageRequest.of(page, size), totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> listAll() {
        return RoleMapper.toDTOList(roleDAO.listAllRoles());
    }

    /**
     * @throws ResourceNotFoundException si no existe el rol.
     */
    @Override
    @Transactional(readOnly = true)
    public RoleUpdateDTO getForEdit(Long id) {
        Role role = roleDAO.getRoleById(id);
        if (role == null) {
            throw new ResourceNotFoundException("role", "id", id);
        }
        return RoleMapper.toUpdateDTO(role);
    }

    /**
     * @throws DuplicateResourceException si ya existe un rol con el mismo nombre.
     */
    @Override
    public void create(RoleCreateDTO dto) {
        if (roleDAO.existsRoleByName(dto.getName())) {
            throw new DuplicateResourceException("role", "name", dto.getName());
        }
        Role role = RoleMapper.toEntity(dto);
        roleDAO.insertRole(role);
        logger.info("Rol {} insertado con éxito.", role.getName());
    }

    /**
     * @throws DuplicateResourceException si otro rol ya usa ese nombre.
     * @throws ResourceNotFoundException  si no existe el rol a actualizar.
     */
    @Override
    public void update(RoleUpdateDTO dto) {
        if (roleDAO.existsRoleByNameAndNotId(dto.getName(), dto.getId())) {
            throw new DuplicateResourceException("role", "name", dto.getName());
        }
        Role role = roleDAO.getRoleById(dto.getId());
        if (role == null) {
            throw new ResourceNotFoundException("role", "id", dto.getId());
        }
        RoleMapper.copyToExistingEntity(dto, role);
        roleDAO.updateRole(role);
        logger.info("Rol con id {} actualizado con éxito.", role.getId());
    }

    /**
     * @throws ResourceNotFoundException si no existe el rol.
     */
    @Override
    public void delete(Long id) {
        if (!roleDAO.existsRoleById(id)) {
            throw new ResourceNotFoundException("role", "id", id);
        }
        roleDAO.deleteRole(id);
        logger.info("Rol con id {} eliminado con éxito.", id);
    }

    /** Cuenta cuántos usuarios tienen asignado el rol. */
    @Override
    @Transactional(readOnly = true)
    public long countUsers(Long id) {
        return roleDAO.countUsersByRoleId(id);
    }

    /**
     * @throws ResourceNotFoundException si no existe el rol.
     */
    @Override
    @Transactional(readOnly = true)
    public RoleDTO getDetail(Long id) {
        Role role = roleDAO.getRoleById(id);
        if (role == null) {
            throw new ResourceNotFoundException("role", "id", id);
        }
        return RoleMapper.toDTO(role);
    }
}
