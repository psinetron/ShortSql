package ru.slybeaver.shortsql;

import ru.slybeaver.shortsql.annotations.DBFieldName;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class POJOConverter {

    POJOConverter() {
    }

    <T> T fromDBCreator(ResultSet resultSet, Class<T> classOfT) throws InvocationTargetException, IllegalAccessException, InstantiationException, SQLException, ShortSQLException {
        T result = classOfT.newInstance();
        for (int i = 0; i < result.getClass().getDeclaredFields().length; i++) {
            for (int i1 = 0; i1 < result.getClass().getDeclaredFields()[i].getDeclaredAnnotations().length; i1++) {
                if (result.getClass().getDeclaredFields()[i].getDeclaredAnnotations()[i1] instanceof DBFieldName) {

                    for (int i2 = 0; i2 < result.getClass().getDeclaredMethods().length; i2++) {
                        boolean isSetterMethod = false;
                        if (Character.isLowerCase(result.getClass().getDeclaredFields()[i].getName().charAt(0)) && Character.isUpperCase(result.getClass().getDeclaredFields()[i].getName().charAt(1))) {
                            if (result.getClass().getDeclaredMethods()[i2].getName().equals("set" + result.getClass().getDeclaredFields()[i].getName())) {
                                isSetterMethod = true;
                            }
                        } else {
                            if (result.getClass().getDeclaredMethods()[i2].getName().equals("set" + Character.toUpperCase(result.getClass().getDeclaredFields()[i].getName().charAt(0)) + result.getClass().getDeclaredFields()[i].getName().substring(1))) {
                                isSetterMethod = true;
                            }
                        }

                        if (isSetterMethod) {
                            int columnIndex = resultSet.findColumn(((DBFieldName) result.getClass().getDeclaredFields()[i].getDeclaredAnnotations()[i1]).value());
                            try {


                                switch (resultSet.getMetaData().getColumnType(columnIndex)) {
                                    case Types.TINYINT:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getByte(columnIndex));
                                        break;
                                    case Types.SMALLINT:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getInt(columnIndex));
                                        break;
                                    case Types.INTEGER:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getInt(columnIndex));
                                        break;
                                    case Types.BIGINT:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getLong(columnIndex));
                                        break;
                                    case Types.FLOAT:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getFloat(columnIndex));
                                        break;
                                    case Types.DOUBLE:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getDouble(columnIndex));
                                        break;
                                    case Types.DECIMAL:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getBigDecimal(columnIndex));
                                        break;
                                    case Types.DATE:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getDate(columnIndex));
                                        break;
                                    case Types.TIME:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getDate(columnIndex));
                                        break;
                                    case Types.TIMESTAMP:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getDate(columnIndex));
                                        break;
                                    case Types.CHAR:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getString(columnIndex));
                                        break;
                                    case Types.BINARY:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getBytes(columnIndex));
                                        break;
                                    case Types.VARCHAR:
                                        result.getClass().getDeclaredMethods()[i2].invoke(result, resultSet.getString(columnIndex));
                                        break;
                                }
                            } catch (Exception e) {
                                throw new ShortSQLException("\nColumn name: \"" + ((DBFieldName) result.getClass().getDeclaredFields()[i].getDeclaredAnnotations()[i1]).value() +
                                        "\"\nObject method: " + result.getClass().getDeclaredMethods()[i2] +
                                        "\nDatabase data: " + resultSet.getObject(columnIndex) +
                                        "\nERROR: ", e.getMessage());
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }
}
