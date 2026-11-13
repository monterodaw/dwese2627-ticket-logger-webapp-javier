package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.*;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador que maneja las operaciones CRUD para la entidad `Region`.
 * Delega la lógica de negocio en `RegionService`; aquí solo se gestiona lo "web":
 * binding, validación del formulario, vistas, redirects y mensajes i18n.
 */
@Controller
@RequestMapping("/regions")
public class RegionController {

    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    @Autowired
    private MessageSource messageSource;

    // Servicio para las operaciones del CRUD de Region
    @Autowired
    private RegionService regionService;

    /**
     * @param page      número de página (0-based)
     * @param size      tamaño de página (nº de elementos por página)
     * @param sortField campo por el que se ordenan los resultados (id,code,name)
     * @param sortDir   direccion de ordenación ("asc" o "desc")
     * @param model     Objeto del modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para renderizar la lista de regiones.
     */
    @GetMapping
    public String listRegions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {

        logger.info("Solicitando la lista de regiones... page={}, size={}, sortField={}, sortDir={}",
                page, size, sortField, sortDir);

        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        try {
            // El controller solo construye el Pageable (algo "web"); la consulta la hace el service
            Sort sort = "desc".equalsIgnoreCase(sortDir)
                    ? Sort.by(sortField).descending()
                    : Sort.by(sortField).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<RegionDTO> listRegionsDTOs = regionService.list(pageable);

            logger.info("Se han cargado {} regiones en la página {}.",
                    listRegionsDTOs.getNumberOfElements(), page);

            model.addAttribute("listRegions", listRegionsDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", listRegionsDTOs.getTotalPages());
            model.addAttribute("totalElements", listRegionsDTOs.getTotalElements());
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");

        } catch (Exception e) {
            logger.error("Error al listar las regiones: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }

        return "views/region/region-list";
    }

    /**
     * Muestra el formulario para crear una nueva región.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva región.");
        model.addAttribute("region", new RegionCreateDTO());
        return "views/region/region-form";
    }

    /**
     * Muestra el formulario de edición de una región.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Mostrando formulario de edición para la región con ID {}", id);

        try {
            // El service encapsula la búsqueda y el mapeo a DTO
            RegionUpdateDTO regionDTO = regionService.getForEdit(id);

            model.addAttribute("region", regionDTO);
            return "views/region/region-form";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la región con ID {}", id);
            String msg = messageSource.getMessage("msg.region.error.notfound", new Object[]{id}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";

        } catch (Exception e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region.error.load", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
    }

    /**
     * Elimina una región de la base de datos.
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Eliminando región con ID {}", id);

        try {
            // El service comprueba existencia y elimina (o lanza ResourceNotFoundException)
            regionService.delete(id);

            logger.info("Región con ID {} eliminada con éxito.", id);
            return "redirect:/regions";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la región con ID {}", id);
            String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/regions";

        } catch (Exception e) {
            logger.error("Error al eliminar la región con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.region-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions";
        }
    }

    /**
     * Inserta una nueva región en la base de datos.
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") RegionCreateDTO regionDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Insertando nueva región con código {}", regionDTO.getCode());

        try {
            // 1) Errores de validación del formulario (Bean Validation)
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            // 2) El service aplica la regla del código único y persiste
            regionService.create(regionDTO);

            logger.info("Región {} insertada con éxito.", regionDTO.getCode());
            return "redirect:/regions";

        } catch (DuplicateResourceException ex) {
            // Caso "semántico": código duplicado
            logger.warn("El código de la región {} ya existe.", regionDTO.getCode());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/new";

        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", regionDTO.getCode(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/new";
        }
    }

    /**
     * Muestra el detalle de una región.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Mostrando detalle de la región con ID {}", id);

        try {
            // El service se encarga de cargar con fetch (provinces) y mapear a DTO
            RegionDetailDTO regionDTO = regionService.getDetail(id);

            model.addAttribute("region", regionDTO);
            return "views/region/region-detail";

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";

        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la región {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
    }

    /**
     * Actualiza una región existente en la base de datos.
     */
    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") RegionUpdateDTO regionDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Actualizando región con ID {}", regionDTO.getId());

        try {
            // 1) Errores de validación del formulario (Bean Validation)
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            // 2) El service aplica reglas (código único), comprueba existencia y actualiza
            regionService.update(regionDTO);

            logger.info("Región con ID {} actualizada con éxito.", regionDTO.getId());
            return "redirect:/regions";

        } catch (DuplicateResourceException ex) {
            logger.warn("El código de la región {} ya existe para otra región.", regionDTO.getCode());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.codeExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/edit?id=" + regionDTO.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la región con ID {}", regionDTO.getId());
            String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/regions";

        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/edit?id=" + regionDTO.getId();
        }
    }
}