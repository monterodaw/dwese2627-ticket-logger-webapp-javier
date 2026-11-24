package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceCreateDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceDetailDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Casos de uso del CRUD de provincias.
 */
public interface ProvinceService {

    /** Lista paginada y ordenable de provincias (con el nombre de su región). */
    Page<ProvinceDTO> list(Pageable pageable);

    /** Lista completa de provincias. */
    List<ProvinceDTO> listAll();

    /** Devuelve los datos de una provincia para precargar el formulario de edición. */
    ProvinceUpdateDTO getForEdit(Long id);

    /** Crea una provincia. Lanza DuplicateResourceException si el código ya existe. */
    void create(ProvinceCreateDTO dto);

    /** Actualiza una provincia. Lanza ResourceNotFoundException o DuplicateResourceException. */
    void update(ProvinceUpdateDTO dto);

    /** Elimina una provincia. Lanza ResourceNotFoundException si no existe. */
    void delete(Long id);

    /** Devuelve el detalle de una provincia, incluyendo su región. */
    ProvinceDetailDTO getDetail(Long id);
}
