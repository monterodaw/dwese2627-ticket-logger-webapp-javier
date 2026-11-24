package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.services;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos.ProvinceDAO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceCreateDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceDetailDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.dtos.ProvinceUpdateDTO;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Province;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.mappers.ProvinceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de la lógica de negocio (casos de uso) para el CRUD de {@link Province}.
 * <p>
 * Esta capa se encarga de:
 * <ul>
 *   <li>Acceder a base de datos mediante {@link ProvinceDAO}.</li>
 *   <li>Aplicar reglas de negocio (por ejemplo, evitar códigos duplicados).</li>
 *   <li>Transformar entidades a DTOs y viceversa mediante {@link ProvinceMapper}.</li>
 *   <li>Lanzar excepciones semánticas ({@link ResourceNotFoundException},
 *       {@link DuplicateResourceException}) para que el controlador decida cómo presentarlas.</li>
 * </ul>
 * Al ser transaccional, la región (carga LAZY) está disponible al construir los DTOs.
 */
@Service
@Transactional
public class ProvinceServiceImpl implements ProvinceService {

    @Autowired
    private ProvinceDAO provinceDAO;

    @Override
    @Transactional(readOnly = true)
    public Page<ProvinceDTO> list(Pageable pageable) {
        // Campo y dirección de ordenación por defecto
        String sortField = "name";
        String sortDir = "asc";
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortField = order.getProperty();
            sortDir = order.getDirection().name().toLowerCase();
        }

        List<Province> provinces = provinceDAO.listProvincesPage(
                pageable.getPageNumber(), pageable.getPageSize(), sortField, sortDir);
        long total = provinceDAO.countProvinces();

        return new PageImpl<>(ProvinceMapper.toDTOList(provinces), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceDTO> listAll() {
        return ProvinceMapper.toDTOList(provinceDAO.listAllProvinces());
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinceUpdateDTO getForEdit(Long id) {
        Province province = provinceDAO.getProvinceById(id);
        if (province == null) {
            throw new ResourceNotFoundException("province", "id", id);
        }
        return ProvinceMapper.toUpdateDTO(province);
    }

    @Override
    public void create(ProvinceCreateDTO dto) {
        if (provinceDAO.existsProvinceByCode(dto.getCode())) {
            throw new DuplicateResourceException("province", "code", dto.getCode());
        }
        Province province = ProvinceMapper.toEntity(dto);
        provinceDAO.insertProvince(province);
    }

    @Override
    public void update(ProvinceUpdateDTO dto) {
        if (provinceDAO.existsProvinceByCodeAndNotId(dto.getCode(), dto.getId())) {
            throw new DuplicateResourceException("province", "code", dto.getCode());
        }

        Province province = provinceDAO.getProvinceById(dto.getId());
        if (province == null) {
            throw new ResourceNotFoundException("province", "id", dto.getId());
        }

        ProvinceMapper.copyToExistingEntity(dto, province);
        provinceDAO.updateProvince(province);
    }

    @Override
    public void delete(Long id) {
        if (provinceDAO.getProvinceById(id) == null) {
            throw new ResourceNotFoundException("province", "id", id);
        }
        provinceDAO.deleteProvince(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinceDetailDTO getDetail(Long id) {
        Province province = provinceDAO.getProvinceById(id);
        if (province == null) {
            throw new ResourceNotFoundException("province", "id", id);
        }
        return ProvinceMapper.toDetailDTO(province);
    }
}