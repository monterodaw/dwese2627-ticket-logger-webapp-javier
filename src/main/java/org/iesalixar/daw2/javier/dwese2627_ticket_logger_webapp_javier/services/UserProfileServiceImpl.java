package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.daos.UserDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.daos.UserProfileDAO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.User;
import org.iesalixar.daw2.javimontero.crud_usuario.entities.UserProfile;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.InvalidFileException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.mappers.UserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lógica de negocio del perfil de usuario (relación 1:1) y de la imagen de perfil.
 * Desde un servicio se puede llamar a otros servicios: aquí se usa {@link FileStorageService}.
 */
@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    /** Límite de tamaño máximo permitido para la imagen (2MB). */
    private static final long MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserProfileDAO userProfileDAO;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Construye el DTO del formulario de perfil para un usuario identificado por email.
     * <ul>
     *     <li>busca el {@code User} por email,</li>
     *     <li>busca su {@code UserProfile} (puede no existir),</li>
     *     <li>mapea {@code User + UserProfile -> UserProfileFormDTO}.</li>
     * </ul>
     *
     * @throws ResourceNotFoundException si no existe el usuario con ese email.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileFormDTO getFormByEmail(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("user", "email", email);
        }
        UserProfile profile = userProfileDAO.getUserProfileByUserId(user.getId());
        return UserProfileMapper.toFormDto(user, profile);
    }

    /**
     * Actualiza o crea el perfil de usuario a partir del DTO del formulario.
     * <ol>
     *     <li>comprueba que el User existe,</li>
     *     <li>carga el UserProfile si existe,</li>
     *     <li>si se adjunta imagen: valida tipo/tamaño, guarda la nueva y borra la anterior,</li>
     *     <li>crea o actualiza la entidad perfil y la persiste.</li>
     * </ol>
     *
     * @throws ResourceNotFoundException si no existe el usuario asociado.
     * @throws InvalidFileException      si la imagen no cumple las validaciones o no se puede guardar.
     */
    @Override
    public void updateProfile(UserProfileFormDTO profileDto, MultipartFile profileImageFile) {
        Long userId = profileDto.getUserId();
        logger.info("Actualizando perfil para userId={}", userId);

        // 1) Comprobar que existe el User
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("user", "id", userId);
        }

        // 2) Cargar perfil (puede no existir)
        UserProfile profile = userProfileDAO.getUserProfileByUserId(userId);
        boolean isNew = (profile == null);

        // 3) Si hay imagen nueva, validar + guardar + borrar anterior
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            // Validaciones semánticas (lanzan InvalidFileException)
            validateProfileImage(profileImageFile);

            String oldImagePath = profileDto.getProfileImage(); // ruta actual (puede ser null/blank)

            String newImageWebPath = fileStorageService.saveFile(profileImageFile);
            if (newImageWebPath == null || newImageWebPath.isBlank()) {
                throw new InvalidFileException(
                        "userProfile",
                        "profileImageFile",
                        profileImageFile.getOriginalFilename(),
                        "No se pudo guardar la imagen de perfil.",
                        "msg.userProfile.image.saveError"
                );
            }
            profileDto.setProfileImage(newImageWebPath);

            // Borrar anterior si existía
            if (oldImagePath != null && !oldImagePath.isBlank()) {
                fileStorageService.deleteFile(oldImagePath);
            }
        }

        // 4) Crear o actualizar entidad de perfil
        if (isNew) {
            profile = UserProfileMapper.toNewEntity(profileDto, user);
            user.setProfile(profile); // mantenemos la relación bidireccional coherente
        } else {
            UserProfileMapper.copyToExistingEntity(profileDto, profile);
        }

        // 5) Persistir
        userProfileDAO.saveOrUpdateUserProfile(profile);
    }

    /**
     * Valida la imagen del perfil:
     * <ul>
     *     <li>tipo MIME debe empezar por {@code image/}</li>
     *     <li>tamaño máximo 2MB</li>
     * </ul>
     *
     * @throws InvalidFileException si no cumple validaciones.
     */
    private void validateProfileImage(MultipartFile file) {
        String contentType = file.getContentType();
        // MIME inválido
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    contentType,
                    "Tipo de archivo no permitido",
                    "msg.userProfile.image.invalidType"
            );
        }
        // Tamaño excedido
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    file.getSize(),
                    "Archivo demasiado grande (máximo " + MAX_IMAGE_SIZE_BYTES + " bytes)",
                    "msg.userProfile.image.tooLarge"
            );
        }
    }
}