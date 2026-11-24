package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;

import java.util.Collection;
import java.util.List;

/**
 * Interfaz DAO para la gestión de la entidad {@link Role} (tabla {@code roles}).
 */
public interface RoleDAO {

    List<Role> listAllRoles();

    /** UD04-3: página de roles ordenada. */
    List<Role> listRolesPage(int page, int size, String sortField, String sortDir);

    long countRoles();

    /** UD04-5: carga los roles cuyos ids llegan desde el select múltiple del formulario de usuario. */
    List<Role> findAllByIds(Collection<Long> ids);

    void insertRole(Role role);

    void updateRole(Role role);

    void deleteRole(Long id);

    Role getRoleById(Long id);

    boolean existsRoleById(Long id);

    boolean existsRoleByName(String name);

    boolean existsRoleByNameAndNotId(String name, Long id);

    /** Cuenta cuántos usuarios tienen asignado el rol indicado. */
    long countUsersByRoleId(Long roleId);
}
