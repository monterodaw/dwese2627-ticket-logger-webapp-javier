package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Province;

import java.util.List;

public interface ProvinceDAO {

    List<Province> listAllProvinces();
    void insertProvince(Province province);
    void updateProvince(Province province);
    void deleteProvince(Long id);
    Province getProvinceById(Long id);
    boolean existsProvinceByCode(String code);
    boolean existsProvinceByCodeAndNotId(String code, Long id);

}

