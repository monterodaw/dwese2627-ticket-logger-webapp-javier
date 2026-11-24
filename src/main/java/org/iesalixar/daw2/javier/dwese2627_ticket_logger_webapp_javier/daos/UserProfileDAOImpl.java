package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.UserProfile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO para la entidad {@link UserProfile}.
 * Implementación usando Hibernate/JPA a través de {@link EntityManager}.
 */
@Repository
@Transactional
public class UserProfileDAOImpl implements UserProfileDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Obtiene el perfil asociado a un usuario por su id.
     *
     * @param userId id del usuario (coincide con user_profiles.user_id).
     * @return el perfil del usuario, o null si no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfile getUserProfileByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        // Shared primary key: userId = PK de UserProfile
        return entityManager.find(UserProfile.class, userId);
    }

    /**
     * Inserta o actualiza el perfil según exista ya en base de datos.
     *
     * @param userProfile entidad a guardar.
     */
    @Override
    public void saveOrUpdateUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return;
        }
        if (userProfile.getId() == null || !existsUserProfileByUserId(userProfile.getId())) {
            // Caso "insert" (nuevo perfil)
            entityManager.persist(userProfile);
        } else {
            // Caso "update" (perfil ya existente)
            entityManager.merge(userProfile);
        }
    }

    /**
     * Indica si existe un perfil asociado al usuario dado.
     *
     * @param userId id del usuario.
     * @return true si existe un perfil para ese usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsUserProfileByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        String jpql = "SELECT COUNT(up) FROM UserProfile up WHERE up.id = :userId";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("userId", userId);
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}
