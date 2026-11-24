package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;

import java.util.List;

/**
 * Interfaz DAO para la gestión de la entidad {@link User} (tabla {@code users})
 * y de su relación N:M con los roles (tabla {@code user_roles}).
 */
public interface UserDAO {

    List<User> listAllUsers();

    /**
     * UD04-3: lista una página de usuarios ordenada.
     *
     * @param page      página actual (0-based).
     * @param size      número de elementos por página.
     * @param sortField campo por el que ordenar (id, email, active, lastPasswordChange...).
     * @param sortDir   dirección de ordenación ("asc" o "desc").
     */
    List<User> listUsersPage(int page, int size, String sortField, String sortDir);

    long countUsers();

    void insertUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

    User getUserById(Long id);

    User getUserByEmail(String email);

    boolean existsUserById(Long id);

    boolean existsUserByEmail(String email);

    boolean existsUserByEmailAndNotId(String email, Long id);
}
