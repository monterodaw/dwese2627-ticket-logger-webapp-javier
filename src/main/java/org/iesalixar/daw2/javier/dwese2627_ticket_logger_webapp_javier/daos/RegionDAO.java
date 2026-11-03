package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Region;

import java.sql.SQLException;
import java.util.List;
public interface RegionDAO {

    List<Region> listAllRegions();
    List<Region> listRegionsPage(int page, int size,String sortField,String sortDir);
    long countRegions();
    void insertRegion(Region region);
    void updateRegion(Region region);
    void deleteRegion(Long id);
    Region getRegionById(Long id);
    boolean existsRegionByCode(String code);
    boolean existsRegionByCodeAndNotId(String code, Long id);

}


