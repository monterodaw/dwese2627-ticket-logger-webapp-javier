package org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.daos;

import org.iesalixar.daw2.javier.dwese2627_ticket_logger_webapp_javier.entities.Region;

import java.sql.SQLException;
import java.util.List;

public interface RegionDAO {
    List<Region> listAllRegions() throws SQLException;
    void insertRegion(Region region) throws SQLException;
    boolean existsRegionByCode(String code) throws SQLException;
    void deleteRegion(Long id) throws SQLException;
    // 💡 AGREGAMOS LOS MÉTODOS PARA QUE FUNCIONE LA EDICIÓN:
    void updateRegion(Region region) throws SQLException;
    Region getRegionById(Long id) throws SQLException;
    boolean existsRegionByCodeAndNotId(String code, Long id) throws SQLException;
}
