package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.iesalixar.daw2.javimontero.crud_usuario.daos.RoleDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Implementación JPA/Hibernate de {@link RoleDAO} usando {@link EntityManager}.
 */
@Repository
@Transactional
public class RoleDAOImpl implements RoleDAO {

    private static final Logger logger = LoggerFactory.getLogger(RoleDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /** Lista todos los roles ordenados por nombre (para el select de usuarios). */
    @Override
    @Transactional(readOnly = true)
    public List<Role> listAllRoles() {
        logger.info("Listing all roles from the database.");
        String hql = "SELECT r FROM Role r ORDER BY r.name";
        List<Role> roles = entityManager.createQuery(hql, Role.class).getResultList();
        logger.info("Retrieved {} roles from the database.", roles.size());
        return roles;
    }

    /**
     * Lista una página de roles ordenada usando CriteriaBuilder.
     *
     * @param page      página actual (0-based).
     * @param size      número de elementos por página.
     * @param sortField campo de ordenación (id, name, displayName).
     * @param sortDir   dirección ("asc" o "desc").
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> listRolesPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing roles page={}, size={}, sortField={}, sortDir={} from the database.",
                page, size, sortField, sortDir);
        int offset = page * size;

        // 1. Construcción de Criteria
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> root = cq.from(Role.class);

        // 2. Determinar el campo de ordenación permitido (whitelist)
        Path<?> sortPath;
        switch (sortField == null ? "" : sortField) {
            case "id" -> sortPath = root.get("id");
            case "name" -> sortPath = root.get("name");
            case "displayName" -> sortPath = root.get("displayName");
            default -> {
                logger.warn("Unknown sortField '{}' for Role, defaulting to 'name'.", sortField);
                sortPath = root.get("name");
            }
        }

        // 3. Dirección de ordenación
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        Order order = descending ? cb.desc(sortPath) : cb.asc(sortPath);

        // 4. Aplicar ordenación a la query
        cq.select(root).orderBy(order);

        // 5. Crear TypedQuery, aplicar paginación y ejecutar
        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    /** Devuelve el número total de roles. */
    @Override
    @Transactional(readOnly = true)
    public long countRoles() {
        String hql = "SELECT COUNT(r) FROM Role r";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
    }

    /** Carga los roles cuyos ids se indican. Si la colección está vacía devuelve lista vacía. */
    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String hql = "SELECT r FROM Role r WHERE r.id IN :ids";
        return entityManager.createQuery(hql, Role.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public void insertRole(Role role) {
        logger.info("Inserting role with name: {}", role.getName());
        entityManager.persist(role);
        logger.info("Inserted role with id: {}", role.getId());
    }

    @Override
    public void updateRole(Role role) {
        logger.info("Updating role with id: {}", role.getId());
        entityManager.merge(role);
        logger.info("Updated role with id: {}", role.getId());
    }

    @Override
    public void deleteRole(Long id) {
        logger.info("Deleting role with id: {}", id);
        Role role = entityManager.find(Role.class, id);
        if (role != null) {
            entityManager.remove(role);
            logger.info("Deleted role with id: {}", id);
        } else {
            logger.warn("Role with id {} not found.", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        logger.info("Retrieving role by id: {}", id);
        Role role = entityManager.find(Role.class, id);
        if (role == null) {
            logger.warn("Role with id {} not found.", id);
        }
        return role;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsRoleById(Long id) {
        String hql = "SELECT COUNT(r) FROM Role r WHERE r.id = :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count != null && count > 0;
    }

    /** Comprueba si existe un rol con ese nombre (sin distinguir mayúsculas). */
    @Override
    @Transactional(readOnly = true)
    public boolean existsRoleByName(String name) {
        logger.info("Checking if role with name: {} exists", name);
        String hql = "SELECT COUNT(r) FROM Role r WHERE UPPER(r.name) = :name";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("name", name.toUpperCase())
                .getSingleResult();
        return count != null && count > 0;
    }

    /** Comprueba si existe OTRO rol (distinto del id indicado) con ese nombre. */
    @Override
    @Transactional(readOnly = true)
    public boolean existsRoleByNameAndNotId(String name, Long id) {
        logger.info("Checking if role with name: {} exists excluding id: {}", name, id);
        String hql = "SELECT COUNT(r) FROM Role r WHERE UPPER(r.name) = :name AND r.id <> :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("name", name.toUpperCase())
                .setParameter("id", id)
                .getSingleResult();
        return count != null && count > 0;
    }

    /** Cuenta los usuarios que tienen el rol (recorriendo la relación N:M). */
    @Override
    @Transactional(readOnly = true)
    public long countUsersByRoleId(Long roleId) {
        String hql = "SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("roleId", roleId)
                .getSingleResult();
        return count != null ? count : 0L;
    }
}
