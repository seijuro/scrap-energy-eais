package com.github.seijuro.scrap.enegery.downloader.db;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class MySQLDBController {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException cnfexcp) {
            cnfexcp.printStackTrace();
        }
    }

    /**
     * Instance Properties
     */
    @NonNull
    protected final String host;
    protected final String port;
    @NonNull
    protected final String user;
    @NonNull
    protected final String passwd;
    protected final String database;

    protected Connection conn = null;
    protected String connectionString = null;

    /**
     * C'tor #1
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     */
    protected MySQLDBController(String $host, String $port, String $user, String $passwd, String $database) {
        //  assertion.
        assert Objects.nonNull($host) && Objects.nonNull($user) && Objects.nonNull($passwd);

        this.host = $host;
        this.port = $port;
        this.user = $user;
        this.passwd = $passwd;
        this.database = $database;
    }

    protected String getJDBCConnectionString() {
        if (Objects.isNull(this.connectionString)) {
            StringBuffer sb = new StringBuffer("jdbc:mysql://");

            sb.append(this.host);
            if (StringUtils.isNotEmpty(this.port))      {   sb.append(":").append(this.port);   }
            if (StringUtils.isNotEmpty(this.database))  {   sb.append("/").append(this.database);   }

            this.connectionString = sb.toString();
        }

        return this.connectionString;
    }

    protected Connection connect() throws SQLException {
        if (Objects.nonNull(this.conn) ||
                this.conn.isClosed()) {
            this.conn = DriverManager.getConnection(getJDBCConnectionString(), this.user, this.passwd);
        }

        return conn;
    }

    protected void close() throws SQLException {
        if (Objects.nonNull(this.conn)) {
            if (!this.conn.isClosed()) {
                this.conn.close();
            }

            this.conn = null;
        }
    }

    public void setAutoCommit(boolean flag) throws SQLException {
        if (Objects.nonNull(this.conn) &&
                !this.conn.isClosed()) {
            this.conn.setAutoCommit(flag);
        }
    }

    public void commit() throws SQLException {
        if (Objects.nonNull(this.conn) &&
                !this.conn.isClosed()) {
            this.conn.commit();
        }
    }
}