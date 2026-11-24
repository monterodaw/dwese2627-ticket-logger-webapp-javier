package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.InvalidFileException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador para la funcionalidad de perfil de usuario ("Mi perfil").
 * <p>
 * No es un CRUD clásico, sino un único formulario que permite crear o
 * actualizar el perfil del usuario.
 */
@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    /**
     * Como aún no hay seguridad (login), siempre se edita el perfil de este usuario.
     */
    private static final String FIXED_EMAIL = "admin@app.local";

    private static final String VIEW_FORM = "views/user-profile/user-profile-form";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserProfileService userProfileService;

    // ─────────────────────────────────────────
    // GET: mostrar formulario de perfil
    // ─────────────────────────────────────────

    /**
     * Muestra el formulario de perfil (alta/edición) del usuario.
     *
     * @param model  modelo para pasar datos a la vista.
     * @param locale locale actual para i18n.
     * @return plantilla Thymeleaf del formulario de perfil.
     */
    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale) {
        logger.info("Mostrando formulario de perfil para el usuario fijo {}", FIXED_EMAIL);
        try {
            UserProfileFormDTO formDto = userProfileService.getFormByEmail(FIXED_EMAIL);
            model.addAttribute("userProfileForm", formDto);
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario para cargar el perfil: {}", ex.getMessage());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
            model.addAttribute("userProfileForm", new UserProfileFormDTO());
            return VIEW_FORM;
        } catch (Exception ex) {
            logger.error("Error inesperado cargando el formulario de perfil: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.userProfile.error", null, locale));
            model.addAttribute("userProfileForm", new UserProfileFormDTO());
            return VIEW_FORM;
        }
    }

    // ─────────────────────────────────────────
    // POST: actualizar/crear perfil
    // ─────────────────────────────────────────

    /**
     * Procesa el envío del formulario de perfil.
     * <ul>
     *     <li>Si Bean Validation falla, se devuelve la vista para mantener el BindingResult.</li>
     *     <li>Si el servicio lanza excepciones semánticas, se traducen a mensajes flash + redirect.</li>
     * </ul>
     *
     * @param profileDto         DTO validado del formulario.
     * @param result             resultado de validación.
     * @param profileImageFile   fichero de imagen opcional.
     * @param redirectAttributes mensajes flash.
     * @param locale             locale actual.
     * @return redirect al formulario.
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
                                BindingResult result,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Actualizando perfil para userId={}", profileDto.getUserId());

        // 1) Si hay errores de Bean Validation, se vuelve a la vista (sin redirect)
        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de perfil para userId={}", profileDto.getUserId());
            return VIEW_FORM;
        }

        try {
            // 2) Delegar lógica de negocio en el service
            userProfileService.updateProfile(profileDto, profileImageFile);

            // 3) Mensaje de éxito
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.userProfile.success", null, locale));
        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo actualizar el perfil porque falta un recurso: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
        } catch (InvalidFileException ex) {
            logger.warn("Imagen de perfil inválida: {}", ex.getMessage());
            // El service indica el motivo (tipo, tamaño o error al guardar) mediante la clave i18n
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(ex.getMessageKey(), null, locale));
        } catch (Exception ex) {
            logger.error("Error inesperado actualizando el perfil: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.userProfile.error", null, locale));
        }
        // 4) Redirect siempre al formulario (patrón PRG)
        return "redirect:/profile/edit";
    }
}