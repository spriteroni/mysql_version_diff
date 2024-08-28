package com.database.common.utils.dbtool;

import java.sql.*;

/**
 * JDBC操作工具类
 */
public class JDBCUtil {
    public static String SourceHost="";
    public static String SourceUsername="";
    public static String SourcePassword="";

    public static String TargetHost="";
    public static String TargetUsername="";
    public static String TargetPassword="";

    static{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getSourceConnection() throws SQLException {
        return DriverManager.getConnection(SourceHost, SourceUsername, SourcePassword);
    }

    public static Connection getTargetConnection() throws SQLException {
        return DriverManager.getConnection(TargetHost, TargetUsername, TargetPassword);
    }


    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection connection, Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
