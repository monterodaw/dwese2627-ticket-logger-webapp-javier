package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserProfileFormDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Casos de uso de "Mi perfil".
 */
public interface UserProfileService {

    UserProfileFormDTO getFormByEmail(String email);

    void updateProfile(UserProfileFormDTO profileDto, MultipartFile profileImageFile);
}
