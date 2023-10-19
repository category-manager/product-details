package com.github.sudarshan.productdetails.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sudarshan.categoryManager.core.clientService.CategoryManagerClient;
import com.github.sudarshan.categoryManager.core.pojo.DefaultCategoryExportData;
import com.github.sudarshan.categoryManager.core.pojo.DefaultCategoryPathExportData;
import com.github.sudarshan.categoryManager.core.sp.Node;
import com.github.sudarshan.categoryManager.core.spi.ICategoryExportData;
import com.github.sudarshan.categoryManager.core.spi.ICategoryPathExportData;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static com.github.sudarshan.productdetails.configs.AppConstants.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Configuration
@Log4j2
public class AppConfig {

    @Autowired
    DataSource dataSource;
    Connection connection;

    @PostConstruct
    public void setup() {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Bean
    public Connection connection() {
        return connection;
    }
    @Bean
    public CategoryManagerClient.CategoryManagerClientBuilder categoryManagerClientBuilder() {
        return CategoryManagerClient
                .getBuilder()
                .configureImport(connection, IMPORT_SQL, getImportRowMapper())
                .configureDbExport(connection,
                        EXPORT_ALL_CATEGORY_SQL,
                        EXPORT_CATEGORY_ALL_PATH_SQL,
                        EXPORT_CATEGORY_SQL,
                        EXPORT_CATEGORY_PATH_SQL,
                        getExportCategoryPsMapper(),
                        getExportCategoryPathPsMapper())
                ;
    }
    private Function<ResultSet, Node> getImportRowMapper() {
        return (rs) -> {
            Node node = new Node();
            try {
                String id = rs.getString("id");
                String name = rs.getString("name");
                var mapper = new ObjectMapper();
                var objNode = mapper.createObjectNode();
                objNode.put("name", name);
                JsonNode data = objNode;
                String[] parentCategoryIds = (String[]) rs.getArray("parent_category_ids").getArray();
                if(Objects.isNull(parentCategoryIds)) {
                    parentCategoryIds = new String[0];
                    log.info("found null as parent for {}",id);
                }
                node.set_id(id);
                node.setChildren(new HashSet<>());
                node.setParents(new HashSet<>(Arrays.asList(parentCategoryIds)));
                node.setData(data);
                return node;
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            return node;
        };
    }

    private static BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> getExportCategoryPathPsMapper() {
        return (ps, data) -> {
            try {
                Connection connection = ps.getConnection();
                DefaultCategoryPathExportData d = (DefaultCategoryPathExportData)data;
                String categoryId = d.getCategoryId();
                List<String> ancestorPaths = d.getAncestorPaths();
                ps.setString(1, categoryId);
                ps.setArray(2, connection.createArrayOf("text", ancestorPaths.toArray()));
                ps.setTimestamp(3, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(4, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch(SQLException sqlException) {
                log.error(sqlException.getMessage());
            }
            return ps;
        } ;
    }

    private static BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> getExportCategoryPsMapper() {
        return (ps, data) -> {
            try {
                var d = (DefaultCategoryExportData)data;
                String categoryId = d.getCategoryId();
                Node node = d.getNode();
                Connection connection = ps.getConnection();
                ps.setString(1, categoryId);
                ps.setString(2, node.getData().toString());
                ps.setArray(3, connection.createArrayOf("text",node.getParents().toArray()));
                ps.setArray(4, connection.createArrayOf("text",node.getChildren().toArray()));
                ps.setTimestamp(5, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(6, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch (SQLException sqlException) {
                log.error(sqlException.getMessage());
            }
            return ps;
        } ;
    }
}
