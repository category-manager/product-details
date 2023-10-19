package com.github.sudarshan.productdetails.configs;
public class AppConstants {
    public static final String IMPORT_SQL = "SELECT * FROM CATEGORY";
    public static final int MIN_TYPEAHEAD_LENGTH = 2;
    public static final int MAX_TYPEAHEAD_SUGGESTIONS = 35;
    public static final String EXPORT_CATEGORY_PATH_SQL = "INSERT INTO CATEGORY_PATH (cat_id,paths,created_at,modified_at)" +
            " VALUES (?, ?, ?, ?) ON CONFLICT (cat_id) DO UPDATE set " +
            "(paths, modified_at)=(EXCLUDED.paths, EXCLUDED.modified_at) returning *";
    public static final String EXPORT_CATEGORY_ALL_PATH_SQL = "INSERT INTO CATEGORY_PATH (cat_id,paths,created_at,modified_at)" +
            " VALUES (?, ?, ?, ?) ON CONFLICT (cat_id) DO UPDATE set " +
            "(paths, modified_at)=(EXCLUDED.paths, EXCLUDED.modified_at)";
    public static final String EXPORT_CATEGORY_SQL = "INSERT INTO CATEGORY (id, data, parent_category_ids, child_category_ids, created_at, modified_at) " +
            "VALUES (?, to_json(?::json), ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE set (data, parent_category_ids, child_category_ids, modified_at)=" +
            "(to_json(EXCLUDED.data::json), EXCLUDED.parent_category_ids, EXCLUDED.child_category_ids , EXCLUDED.modified_at) returning *";

    public static final String EXPORT_ALL_CATEGORY_SQL = "INSERT INTO CATEGORY (id, data, parent_category_ids, child_category_ids, created_at, modified_at) " +
            "VALUES (?, to_json(?::json), ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE set (data, parent_category_ids, child_category_ids, modified_at)=" +
            "(to_json(EXCLUDED.data::json), EXCLUDED.parent_category_ids, EXCLUDED.child_category_ids , EXCLUDED.modified_at)";

}
