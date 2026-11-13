package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.RegionCreateDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.RegionDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.RegionDetailDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.RegionUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RegionService {

    Page<RegionDTO> list(Pageable pageable);

    @Transactional(readOnly = true)
    List<RegionDTO> listAll();

    RegionUpdateDTO getForEdit(Long id);

    void create(RegionCreateDTO dto);

    void update(RegionUpdateDTO dto);

    void delete(Long id);

    RegionDetailDTO getDetail(Long id);
}
