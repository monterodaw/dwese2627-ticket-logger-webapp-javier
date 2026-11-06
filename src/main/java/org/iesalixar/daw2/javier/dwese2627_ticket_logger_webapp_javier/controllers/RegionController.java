package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos.ProvinceDAO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos.RegionDAO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.*;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Province;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Region;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers.ProvinceMapper;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers.RegionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

/**
 * Controlador que maneja las operaciones CRUD para la entidad `Region`.
 * Utiliza `RegionDAO` para interactuar con la base de datos.
 */
@Controller
@RequestMapping("/regions")
public class RegionController {

    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProvinceDAO provinceDAO;

    /**
     * @param page número de página (0-based)
     * @param size tamaño de página (nº de elementos por página)
     * @param sortField campo por el que se ordenan los resultados (id,code,name)
     * @param sortDir direccion de ordenación ("asc" o "desc")
     * @param model Objeto del modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para renderizar la lista de regiones.
     */
    @GetMapping
    public String listRegions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {

        logger.info("Solicitando la lista de regiones... page={}, size={},sortField={},sortDir={}", page, size, sortField, sortDir);

        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        try {
            long totalElements = regionDAO.countRegions();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }

            List<Region> listRegions = regionDAO.listRegionsPage(page, size, sortField, sortDir);
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);

            logger.info("Se han cargado {} regiones en la página {}.", listRegionsDTOs.size(), page);

            model.addAttribute("listRegions", listRegionsDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);
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
     * Muestra el formulario para editar una región existente.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);
        Region region = null;
        RegionUpdateDTO regionDTO = null;
        try {
            region = regionDAO.getRegionById(id);
            if (region == null) {
                logger.warn("No se encontró la región con ID {}", id);
            }
            regionDTO = RegionMapper.toUpdateDTO(region);
        } catch (Exception e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la región.");
        }

        model.addAttribute("region", regionDTO);
        return "views/region/region-form";
    }

    /**
     * Elimina una región de la base de datos.
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando región con ID {}", id);
        try {
            regionDAO.deleteRegion(id);
            logger.info("Región con ID {} eliminada con éxito.", id);
            redirectAttributes.addFlashAttribute("successMessage", "Región eliminada con éxito.");
        } catch (Exception e) {
            logger.error("Error al eliminar la región con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la región.");
        }
        return "redirect:/regions";
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

        if (result.hasErrors()) {
            return "views/region/region-form";
        }

        try {
            // Se instancia la clase porque toEntity no es un método estático
            Region region = new RegionMapper().toEntity(regionDTO);
            regionDAO.insertRegion(region);

            String successMessage = messageSource.getMessage("msg.region-controller.insert.success", null, "Región creada con éxito.", locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            logger.error("Error al insertar la región con código {}: {}", regionDTO.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, "Error al crear la región.", locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "views/region/region-form";
        }

        return "redirect:/regions";
    }
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la región con ID {}", id);
        try {
            Province province = provinceDAO.getProvinceById(id);
            ProvinceDetailDTO provinceDTO = ProvinceMapper.toDetailDTO(province);
            if(province == null) {
                String msg = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces";
            }
            model.addAttribute("province", provinceDTO);
            return "views/province/province-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la provincia {} : {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.province-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
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

        if (result.hasErrors()) {
            return "views/region/region-form";
        }

        try {
            // Se instancia la clase porque toEntity no es un método estático
            Region region = new RegionMapper().toEntity(regionDTO);
            regionDAO.updateRegion(region);

            String successMessage = messageSource.getMessage("msg.region-controller.update.success", null, "Región actualizada con éxito.", locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, "Error al actualizar la región.", locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "views/region/region-form";
        }

        return "redirect:/regions";
    }
}
