package com.redblade.i18n.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redblade.i18n.domain.I18nText;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * I18nText 类型处理器 - Oracle 版本
 * 用于 Oracle 数据库的 CLOB/JSON 字段处理
 */
@MappedTypes(I18nText.class)
public class I18nTextOracleTypeHandler extends BaseTypeHandler<I18nText> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, I18nText parameter, JdbcType jdbcType)
        throws SQLException {
        String json = toJson(parameter);
        ps.setString(i, json);
    }

    @Override
    public I18nText getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Clob clob = rs.getClob(columnName);
        if (clob == null) {
            return null;
        }
        String json = clob.getSubString(1, (int) clob.length());
        return parseJson(json);
    }

    @Override
    public I18nText getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Clob clob = rs.getClob(columnIndex);
        if (clob == null) {
            return null;
        }
        String json = clob.getSubString(1, (int) clob.length());
        return parseJson(json);
    }

    @Override
    public I18nText getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Clob clob = cs.getClob(columnIndex);
        if (clob == null) {
            return null;
        }
        String json = clob.getSubString(1, (int) clob.length());
        return parseJson(json);
    }

    private String toJson(I18nText i18nText) {
        if (i18nText == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(i18nText.toMap());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize I18nText to JSON", e);
        }
    }

    private I18nText parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> map = OBJECT_MAPPER.readValue(json, java.util.Map.class);
            return I18nText.fromMap(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse I18nText from JSON: " + json, e);
        }
    }
}