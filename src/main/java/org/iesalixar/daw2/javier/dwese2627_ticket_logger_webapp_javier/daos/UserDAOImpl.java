package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación JPA/Hibernate de {@link UserDAO} usando {@link EntityManager}.
 * <p>
 * La tabla intermedia user_roles la gestiona Hibernate automáticamente a partir
 * del {@code Set<Role>} de la entidad User, por lo que aquí no hay SQL a mano.
 */
@Repository
@Transactional
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lista todos los usuarios con sus roles (JOIN FETCH para evitar LazyInitialization).
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> listAllUsers() {
        logger.info("Listing all users with their roles from the database.");
        String hql = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles ORDER BY u.id";
        List<User> users = entityManager.createQuery(hql, User.class).getResultList();
        logger.info("Retrieved {} users from the database.", users.size());
        return users;
    }

    /**
     * Lista una página de usuarios ordenada.
     * <p>
     * Se usa un CriteriaBuilder para parametrizar la ordenación. Solo se permiten
     * los campos de la whitelist (si llega otro, se ordena por id).
     * No se hace JOIN FETCH de los roles porque Hibernate no puede paginar en BD
     * una consulta con fetch de colecciones; los roles se cargan después (lazy)
     * dentro de la transacción del servicio.
     *
     * @param page      página actual (0-based).
     * @param size      número de elementos por página.
     * @param sortField campo por el que se ordenan los resultados.
     * @param sortDir   dirección de ordenación ("asc" o "desc").
     * @return lista de usuarios de esa página.
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> listUsersPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing users page={}, size={}, sortField={}, sortDir={} from the database.",
                page, size, sortField, sortDir);
        int offset = page * size;

        // 1. Construcción de Criteria
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        // 2. Determinar el campo de ordenación permitido (whitelist)
        Path<?> sortPath;
        switch (sortField == null ? "" : sortField) {
            case "id" -> sortPath = root.get("id");
            case "email" -> sortPath = root.get("email");
            case "active" -> sortPath = root.get("active");
            case "accountNonLocked" -> sortPath = root.get("accountNonLocked");
            case "lastPasswordChange" -> sortPath = root.get("lastPasswordChange");
            case "passwordExpiresAt" -> sortPath = root.get("passwordExpiresAt");
            case "failedLoginAttempts" -> sortPath = root.get("failedLoginAttempts");
            case "emailVerified" -> sortPath = root.get("emailVerified");
            case "mustChangePassword" -> sortPath = root.get("mustChangePassword");
            default -> {
                logger.warn("Unknown sortField '{}' for User, defaulting to 'id'.", sortField);
                sortPath = root.get("id");
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

    /**
     * Devuelve el número total de usuarios.
     */
    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        String hql = "SELECT COUNT(u) FROM User u";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
    }

    /**
     * Inserta un nuevo usuario. Hibernate inserta también sus filas en user_roles.
     */
    @Override
    public void insertUser(User user) {
        logger.info("Inserting user with email: {}", user.getEmail());
        entityManager.persist(user);
        logger.info("Inserted user with id: {}", user.getId());
    }

    /**
     * Actualiza un usuario existente (y sus roles).
     */
    @Override
    public void updateUser(User user) {
        logger.info("Updating user with id: {}", user.getId());
        entityManager.merge(user);
        logger.info("Updated user with id: {}", user.getId());
    }

    /**
     * Elimina un usuario. Hibernate borra sus filas de user_roles y,
     * gracias a cascade + orphanRemoval, también su perfil.
     */
    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
            logger.info("Deleted user with id: {}", id);
        } else {
            logger.warn("User with id {} not found.", id);
        }
    }

    /**
     * Busca un usuario por su id.
     *
     * @return el usuario o null si no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        logger.info("Retrieving user by id: {}", id);
        User user = entityManager.find(User.class, id);
        if (user != null) {
            logger.info("User retrieved: {} - {}", user.getId(), user.getEmail());
        } else {
            logger.warn("User with id {} not found.", id);
        }
        return user;
    }

    /**
     * UD04-4: busca un usuario por su email (sin distinguir mayúsculas).
     *
     * @return el usuario o null si no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        logger.info("Retrieving user by email: {}", email);
        String hql = "SELECT u FROM User u WHERE UPPER(u.email) = :email";
        List<User> users = entityManager.createQuery(hql, User.class)
                .setParameter("email", email.toUpperCase())
                .setMaxResults(1)
                .getResultList();
        if (users.isEmpty()) {
            logger.warn("User with email {} not found.", email);
            return null;
        }
        return users.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsUserById(Long id) {
        String hql = "SELECT COUNT(u) FROM User u WHERE u.id = :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Verifica si existe un usuario con el email indicado (ignorando mayúsculas/minúsculas).
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsUserByEmail(String email) {
        logger.info("Checking if user with email: {} exists", email);
        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) = :email";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("email", email.toUpperCase())
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("User with email: {} exists: {}", email, exists);
        return exists;
    }

    /**
     * Verifica si existe otro usuario (distinto del id indicado) con ese email.
     * Se usa en la edición para que un usuario pueda conservar su propio email.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsUserByEmailAndNotId(String email, Long id) {
        logger.info("Checking if user with email: {} exists excluding id: {}", email, id);
        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) = :email AND u.id <> :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("email", email.toUpperCase())
                .setParameter("id", id)
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("User with email: {} exists excluding id {}: {}", email, id, exists);
        return exists;
    }
}
