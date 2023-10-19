package com.github.sudarshan.productdetails.repositories;

import com.github.sudarshan.productdetails.models.TypeaheadDatum;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
@Log4j2
public class TypeaheadRepository {

    @Autowired
    Connection connection;
    private static final String TYPEAHEAD_IMPORT_QUERY = "SELECT ID, NAME FROM CATEGORY";
    public List<TypeaheadDatum> getTypeaheadData() {
        List<TypeaheadDatum> result = new ArrayList<>();
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TYPEAHEAD_IMPORT_QUERY);
            while(rs.next()) {
                TypeaheadDatum data = new TypeaheadDatum();
                data.setId(rs.getString("id"));
                data.setName(rs.getString("name"));
                result.add(data);
            }
        } catch(SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        log.info("Imported {} typeahead data", result.size());
        return result;
    }

}
