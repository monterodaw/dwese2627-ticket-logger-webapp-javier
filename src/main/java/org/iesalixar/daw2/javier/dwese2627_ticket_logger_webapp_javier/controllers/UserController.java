package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserDetailDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.UserUpdateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.services.RoleService;
import org.iesalixar.daw2.javimontero.crud_usuario.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador del CRUD de usuarios, accesible a través de la URL {@code /users}.
 * <p>
 * Solo hace cosas "de web": binding y validación de formularios, elegir vista o
 * redirect y mensajes i18n. La lógica de negocio está en {@link UserService}, que
 * lanza excepciones de dominio que aquí se traducen a mensajes para el usuario.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /** Vista del formulario de alta/edición. */
    private static final String VIEW_FORM = "views/user/user-form";

    @Autowired
    private UserService userService;

    // Servicio de roles para cargar las opciones del select múltiple
    @Autowired
    private RoleService roleService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Listado paginado y ordenado de usuarios.
     *
     * @param page      número de página (0-based).
     * @param size      tamaño de página (nº de elementos por página).
     * @param sortField campo por el que se ordena (id, email, active...).
     * @param sortDir   dirección de ordenación ("asc" o "desc").
     * @param model     modelo para pasar datos a la vista.
     * @param locale    configuración regional (i18n).
     * @return plantilla del listado de usuarios.
     */
    @GetMapping
    public String listUsers(@RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size,
                            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
                            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                            Model model,
                            Locale locale) {
        logger.info("Solicitando la lista de usuarios... page={}, size={}, sortField={}, sortDir={}",
                page, size, sortField, sortDir);
        try {
            Page<UserDTO> pageUsers = userService.list(page, size, sortField, sortDir);
            logger.info("Se han cargado {} usuarios en la página {}.", pageUsers.getNumberOfElements(), pageUsers.getNumber());

            model.addAttribute("listUsers", pageUsers.getContent());
            model.addAttribute("currentPage", pageUsers.getNumber());
            model.addAttribute("pageSize", pageUsers.getSize());
            model.addAttribute("totalPages", pageUsers.getTotalPages());
            model.addAttribute("totalElements", pageUsers.getTotalElements());
        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage(), e);
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.list.error", null, locale));
            model.addAttribute("listUsers", java.util.List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0L);
        }
        // Para que la vista pueda construir los enlaces de ordenación
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        return "views/user/user-list";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new UserCreateDTO());
        model.addAttribute("allRoles", roleService.listAll());
        return VIEW_FORM;
    }

    /**
     * Inserta un nuevo usuario.
     *
     * @param userDTO            datos del formulario.
     * @param result             resultado de la validación.
     * @param model              modelo para reenviar datos a la vista en caso de error.
     * @param redirectAttributes atributos para mensajes flash.
     * @param locale             locale actual para mensajes traducidos.
     * @return redirección al listado o el formulario si hay errores.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nuevo usuario con email {}", userDTO.getEmail());

        // Si hay errores de validación (incluidos roles obligatorios), volvemos al formulario
        if (result.hasErrors()) {
            // Es importante recargar la lista de roles para el <select multiple>
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        }

        try {
            userService.create(userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.insert.success", null, locale));
        } catch (DuplicateResourceException ex) {
            logger.warn("El email {} ya existe.", userDTO.getEmail());
            result.rejectValue("email", "msg.user.email.exists");
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            logger.warn("Algún rol seleccionado no existe: {}", ex.getMessage());
            result.rejectValue("roleIds", "msg.user.roles.notfound");
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.insert.error", null, locale));
        }
        return "redirect:/users";
    }

    /**
     * Muestra el formulario para editar un usuario existente.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);
        try {
            // El servicio encapsula la búsqueda y el mapeo a DTO
            UserUpdateDTO userDTO = userService.getForEdit(id);
            model.addAttribute("user", userDTO);
            // Siempre enviar la lista de roles para el <select multiple>
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario con ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.error", null, locale));
            return "redirect:/users";
        }
    }

    /**
     * Actualiza un usuario existente.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando usuario con ID {}", userDTO.getId());

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        }

        try {
            userService.update(userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.update.success", null, locale));
        } catch (DuplicateResourceException ex) {
            logger.warn("El email {} ya existe para otro usuario.", userDTO.getEmail());
            result.rejectValue("email", "msg.user.email.exists");
            model.addAttribute("allRoles", roleService.listAll());
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            if ("role".equals(ex.getResource())) {
                result.rejectValue("roleIds", "msg.user.roles.notfound");
                model.addAttribute("allRoles", roleService.listAll());
                return VIEW_FORM;
            }
            logger.warn("No se encontró el usuario con ID {}", userDTO.getId());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.update.error", null, locale));
        }
        return "redirect:/users";
    }

    /**
     * Elimina un usuario (sus roles y su perfil se eliminan en cascada).
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.delete.success", null, locale));
        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario con ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.delete.error", null, locale));
        }
        return "redirect:/users";
    }

    /**
     * Muestra el detalle de un usuario (roles y perfil incluidos).
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle del usuario con ID {}", id);
        try {
            UserDetailDTO userDTO = userService.getDetail(id);
            model.addAttribute("user", userDTO);
            return "views/user/user-detail";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle del usuario {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.error", null, locale));
            return "redirect:/users";
        }
    }
}
