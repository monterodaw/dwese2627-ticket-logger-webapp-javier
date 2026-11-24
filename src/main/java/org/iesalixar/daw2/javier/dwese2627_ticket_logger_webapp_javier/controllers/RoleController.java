package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleCreateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.dtos.RoleUpdateDTO;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javimontero.crud_usuario.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javimontero.crud_usuario.services.RoleService;
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
 * Controlador del CRUD de roles, accesible a través de la URL {@code /roles}.
 * <p>
 * Desde aquí NO se asignan usuarios a un rol: la asignación se gestiona
 * exclusivamente desde el CRUD de usuarios.
 */
@Controller
@RequestMapping("/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    /** Vista del formulario de alta/edición. */
    private static final String VIEW_FORM = "views/role/role-form";

    // Servicio para las operaciones del CRUD de Role
    @Autowired
    private RoleService roleService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Listado paginado y ordenado de roles.
     */
    @GetMapping
    public String listRoles(@RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size,
                            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
                            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                            Model model,
                            Locale locale) {
        logger.info("Solicitando la lista de roles... page={}, size={}, sortField={}, sortDir={}",
                page, size, sortField, sortDir);
        try {
            Page<RoleDTO> pageRoles = roleService.list(page, size, sortField, sortDir);
            model.addAttribute("listRoles", pageRoles.getContent());
            model.addAttribute("currentPage", pageRoles.getNumber());
            model.addAttribute("pageSize", pageRoles.getSize());
            model.addAttribute("totalPages", pageRoles.getTotalPages());
            model.addAttribute("totalElements", pageRoles.getTotalElements());
        } catch (Exception e) {
            logger.error("Error al listar los roles: {}", e.getMessage(), e);
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.list.error", null, locale));
            model.addAttribute("listRoles", java.util.List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0L);
        }
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        return "views/role/role-list";
    }

    /** Muestra el formulario para crear un nuevo rol. */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo rol.");
        model.addAttribute("role", new RoleCreateDTO());
        return VIEW_FORM;
    }

    /** Inserta un nuevo rol. */
    @PostMapping("/insert")
    public String insertRole(@Valid @ModelAttribute("role") RoleCreateDTO roleDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nuevo rol con nombre {}", roleDTO.getName());
        if (result.hasErrors()) {
            return VIEW_FORM;
        }
        try {
            roleService.create(roleDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.insert.success", null, locale));
        } catch (DuplicateResourceException ex) {
            logger.warn("El rol {} ya existe.", roleDTO.getName());
            result.rejectValue("name", "msg.role.name.exists");
            return VIEW_FORM;
        } catch (Exception e) {
            logger.error("Error al insertar el rol {}: {}", roleDTO.getName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.insert.error", null, locale));
        }
        return "redirect:/roles";
    }

    /** Muestra el formulario de edición de un rol existente. */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para el rol con ID {}", id);
        try {
            RoleUpdateDTO roleDTO = roleService.getForEdit(id);
            model.addAttribute("role", roleDTO);
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el rol con ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.notfound", null, locale));
            return "redirect:/roles";
        } catch (Exception e) {
            logger.error("Error al obtener el rol con ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.error", null, locale));
            return "redirect:/roles";
        }
    }

    /** Actualiza un rol existente. */
    @PostMapping("/update")
    public String updateRole(@Valid @ModelAttribute("role") RoleUpdateDTO roleDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando rol con ID {}", roleDTO.getId());
        if (result.hasErrors()) {
            return VIEW_FORM;
        }
        try {
            roleService.update(roleDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.update.success", null, locale));
        } catch (DuplicateResourceException ex) {
            logger.warn("El nombre de rol {} ya existe para otro rol.", roleDTO.getName());
            result.rejectValue("name", "msg.role.name.exists");
            return VIEW_FORM;
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.notfound", null, locale));
        } catch (Exception e) {
            logger.error("Error al actualizar el rol con ID {}: {}", roleDTO.getId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.update.error", null, locale));
        }
        return "redirect:/roles";
    }

    /** Elimina un rol. No se permite eliminar un rol asignado a algún usuario. */
    @PostMapping("/delete")
    public String deleteRole(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando rol con ID {}", id);
        try {
            // No se permite borrar un rol que esté asignado a algún usuario
            long usersWithRole = roleService.countUsers(id);
            if (usersWithRole > 0) {
                logger.warn("No se puede eliminar el rol {}: asignado a {} usuarios", id, usersWithRole);
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("msg.role-controller.delete.inUse", null, locale));
                return "redirect:/roles";
            }
            roleService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.delete.success", null, locale));
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.notfound", null, locale));
        } catch (Exception e) {
            logger.error("Error al eliminar el rol con ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.delete.error", null, locale));
        }
        return "redirect:/roles";
    }

    /** Muestra el detalle de un rol. */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle del rol con ID {}", id);
        try {
            model.addAttribute("role", roleService.getDetail(id));
            return "views/role/role-detail";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.notfound", null, locale));
            return "redirect:/roles";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle del rol {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.error", null, locale));
            return "redirect:/roles";
        }
    }
}
