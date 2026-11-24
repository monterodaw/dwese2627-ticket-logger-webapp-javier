package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javimontero.crud_usuario.entities.UserProfile;

/**
 * DAO para la entidad {@link UserProfile}.
 * Pensado para la funcionalidad de gestión de perfil de usuario ("Mi perfil"),
 * donde normalmente se trabaja con un único perfil asociado a un usuario.
 */
public interface UserProfileDAO {

    UserProfile getUserProfileByUserId(Long userId);

    void saveOrUpdateUserProfile(UserProfile userProfile);

    boolean existsUserProfileByUserId(Long userId);
}
