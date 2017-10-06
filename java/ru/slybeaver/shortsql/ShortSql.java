package ru.slybeaver.shortsql;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShortSql {

    private Connection connection;
    private String dburl;
    private String dbuser;
    private String dbpassword;
    private Map<String, Object> paramsMap = new HashMap<>();
    private ArrayList<Object> paramsArr = new ArrayList<>();
    private String query = null;

    public ShortSql(String url, String user, String password) {
        this.dburl = url;
        this.dbuser = user;
        this.dbpassword = password;
    }

    public ShortSql query(String query) {
        this.query = query;
        paramsArr.clear();
        paramsMap.clear();
        return this;

    }

    public ShortSql addParam(String paramName, Object paramValue) {
        paramsMap.put(paramName, paramValue);
        return this;
    }

    public ShortSql addParam(Object paramValue) {
        paramsArr.add(paramValue);
        return this;
    }

    public ShortSql addParams(List<Object> params){
        paramsArr.addAll(params);
        return this;
    }

    public ShortSql addParams(Map<String, Object> params) {
        paramsMap.putAll(params);
        return this;
    }

    public <T> List<T> select(Class<T> tClass) throws ShortSQLException {
        ArrayList<T> result = new ArrayList<>();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            connection = DriverManager.getConnection(
                    dburl,
                    dbuser,
                    dbpassword);

            ArrayList<Object> remapedQuery =  remapQuery();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            prepareDatas(preparedStatement, remapedQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(new POJOConverter().fromDBCreator(resultSet, tClass));
            }
            connection.close();
            return result;
        } catch (Exception e) {
            throw new ShortSQLException(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }

    }

    private ArrayList<Object> remapQuery() throws ShortSQLException {
        ArrayList<Object> remapedParams = new ArrayList<>();
        Pattern p = Pattern.compile("(\\?)|(:\\S+)");
        Matcher m = p.matcher(query);
        int arrParamCount = 0;
        while (m.find()) {
            if (m.group(0).equals("?")) {
                if (arrParamCount >= paramsArr.size()) {
                    throw new ShortSQLException("Not enough parameters. Query has more '?' than " + (arrParamCount));
                }
                remapedParams.add(paramsArr.get(arrParamCount));
                arrParamCount++;
            } else {
                if (paramsMap.containsKey(m.group(0).replaceAll(":", ""))) {
                    remapedParams.add(paramsMap.get(m.group(0).replaceAll(":", "")));
                } else {
                    throw new ShortSQLException("Missing parameter: " + m.group(0));
                }
            }
        }
        query = query.replaceAll(":\\S+", "?");
        return remapedParams;
    }

    private void prepareDatas(PreparedStatement preparedStatement, ArrayList<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) instanceof Integer) {
                preparedStatement.setInt(i + 1, (Integer) params.get(i));
            } else if (params.get(i) instanceof Long) {
                preparedStatement.setLong(i + 1, (Long) params.get(i));
            } else if (params.get(i) instanceof Byte) {
                preparedStatement.setByte(i + 1, (Byte) params.get(i));
            } else if (params.get(i) instanceof Boolean) {
                preparedStatement.setBoolean(i + 1, (Boolean) params.get(i));
            } else if (params.get(i) instanceof Double) {
                preparedStatement.setDouble(i + 1, (Double) params.get(i));
            } else if (params.get(i) instanceof Float) {
                preparedStatement.setFloat(i + 1, (Float) params.get(i));
            } else if (params.get(i) instanceof BigDecimal) {
                preparedStatement.setBigDecimal(i + 1, (BigDecimal) params.get(i));
            } else if (params.get(i) instanceof String) {
                preparedStatement.setString(i + 1, (String) params.get(i));
            }
        }
    }
}
