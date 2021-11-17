package io.github.mrriptide.peakcraft.util;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.mrriptide.peakcraft.PeakCraft;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class MySQLHelper {
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static MysqlDataSource getDataSource() throws SQLException{
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();

        FileConfiguration config = PeakCraft.instance.getConfig();

        dataSource.setServerName(config.getString("database.host"));
        dataSource.setPortNumber(config.getInt("database.port"));
        dataSource.setDatabaseName(config.getString("database.name"));
        dataSource.setUser(config.getString("database.user"));
        dataSource.setPassword(config.getString("database.password"));

        testDataSource(dataSource);

        return dataSource;
    }

    private static void testDataSource(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }

    public static boolean tableExists(String tableName) throws SQLException {
        return tableExists(getConnection(), tableName);
    }

    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(*) "
                + "FROM information_schema.tables "
                + "WHERE table_name = ?"
                + "LIMIT 1;");
        preparedStatement.setString(1, tableName);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) != 0;
    }


    public static String getPlaceholders(int count){
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < count; i++){
            if (i != 0){
                builder.append(",");
            }
            builder.append("?");
        }
        return builder.append(")").toString();
    }


    public static void bulkInsert(Connection connection, String queryStart, ArrayList<Object[]> values) throws SQLException {
        String placeholders = getPlaceholders(values.get(0).length);

        StringBuilder builder = new StringBuilder(queryStart);

        for (int i = 0; i < values.size(); i++){
            if (i != 0){
                builder.append(",");
            }
            builder.append(placeholders);
        }

        PreparedStatement statement = connection.prepareStatement(builder.toString());

        int parameterIndex = 1;
        for (Object[] valueSet : values){
            for (Object value : valueSet){
                statement.setObject(parameterIndex++, value);
            }
        }

        statement.execute();
        statement.close();
    }
}
