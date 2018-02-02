package com.github.seijuro.scrap.enegery.downloader.app.conf;

import com.github.seijuro.scrap.enegery.downloader.app.option.AppOption;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    /**
     * Class Instance(s)
     */
    private static Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
    static final String DefaultConfigDirectoryName = "conf";
    static final String DefaultDownloadDirectoryName = "download";
    static final String AppConfigFilename = "app.conf";
    static final String DatabaseConfigFilename = "db.conf";

    /**
     * Singleton Instance
     */
    private static ConfigManager gInstance = null;

    /**
     * Singleton Method
     *
     * @return
     */
    synchronized public static ConfigManager getInstance() {
        if (gInstance == null) {
            gInstance = new ConfigManager();
        }

        return gInstance;
    }

    /**
     * Instance Properties
     */
    private boolean initialized = false;
    private Map<Config, String> dbConfigs = null;
    private Map<Config, String> appConfigs = null;
    private Map<AppOption, String> appOptions = null;

    /**
     * clear <code>ConfManager</code> instance.
     */
    private void clear() {
        this.dbConfigs = null;
        this.appOptions = null;
    }

    /**
     * <code>ConfManager</code> instance must be initialized before use.
     * Threrefore, all interfaces provided by instance should check whethere instance is initialized or not.
     * If call instance method without initialiing, it willl throw <code>NotInitializedException</code>.
     * This method is implement to check & throw exception if not initialized.
     *
     * @throws NotInitializedException
     */
    private void throwExceptionIfNotInitialized() throws NotInitializedException {
        if (!this.initialized) {
            throw new NotInitializedException("ConfManager isn't initialized yet.");
        }
    }

    /**
     * Application option, 'conf', is not required.
     * If user didn't specified, this methods would be called to find (default) 'conf' directory.
     *
     * @param home
     * @return
     */
    private String getDefaultConfigDirectory(String home) {
        return String.format("%s%s%s", home, home.endsWith(File.separator) ? "" : File.separator, DefaultConfigDirectoryName);
    }

    /**
     * Application option, 'download', is not required.
     * If user didn't specified, this method would be called to find (default) 'download' directory.
     *
     * @param home
     * @return
     */
    private String getDefaultDownloadDirectory(String home) {
        return String.format("%s%s%s", home, home.endsWith(File.separator) ? "" : File.separator, DefaultDownloadDirectoryName);
    }

    /**
     * retrieve 'home' direotory path from application options.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getHomeDirectory() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.appOptions);

        return this.appOptions.get(AppOption.HOME);
    }

    /**
     * retrieve 'conf' directory from application options.
     * If not set, this will return default 'conf' directory path.
     * Default 'conf' directory path is '$HOME/conf'.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getConfDireoctory() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.appOptions);

        return this.appOptions.getOrDefault(AppOption.CONF, getDefaultConfigDirectory(getHomeDirectory()));
    }

    /**
     * retrieve 'download' directory from application options.
     * If not set, this will return default 'conf' directory path.
     * Default 'conf' directory path is '$HOME/download'.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDownloadDirecotry() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.appOptions);

        return this.appOptions.getOrDefault(AppOption.DONWLOAD,  getDefaultDownloadDirectory(getHomeDirectory()));
    }

    /**
     * retrieve 'user' information from database configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDatabaseUser() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.dbConfigs);

        String user = this.dbConfigs.getOrDefault(DBConfig.USER, StringUtils.EMPTY);

        if (StringUtils.isEmpty(user)) {
            //  Log
            LOG.warn("Database config doens't contain 'user' information.");
        }

        return user;
    }

    /**
     * retrieve 'passwod' information from database configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDatabasePassword() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.dbConfigs);

        String passwd = this.dbConfigs.getOrDefault(DBConfig.PASSWORD, StringUtils.EMPTY);

        if (StringUtils.isEmpty(passwd)) {
            LOG.warn("Database config. doens't contain 'password' information");
        }

        return passwd;
    }

    /**
     * retrieve 'host' information from database configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDatabaseHost() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.dbConfigs);

        String host = this.dbConfigs.getOrDefault(DBConfig.HOST, StringUtils.EMPTY);

        if (StringUtils.isEmpty(host)) {
            LOG.warn("Database config. doens't contain 'host' information");
        }

        return host;
    }

    /**
     * retrieve 'port' information from database configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDatabasePort() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.dbConfigs);

        String port = this.dbConfigs.getOrDefault(DBConfig.PORT, StringUtils.EMPTY);

        if (StringUtils.isEmpty(port)) {
            LOG.warn("Database config. doens't contain 'port' information");
        }

        return port;
    }

    /**
     * retrieve 'database' information from database configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getDatabaseName() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.dbConfigs);

        String database = this.dbConfigs.getOrDefault(DBConfig.DATABASE, StringUtils.EMPTY);

        if (StringUtils.isEmpty(database)) {
            LOG.warn("Database config. doens't contain 'database' information");
        }

        return database;
    }

    /**
     * generate JDBC connection string to MySQL.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getMySQLJDBConnectionString() throws NotInitializedException {
        String host = getDatabaseHost();
        String port = getDatabasePort();
        String database = getDatabaseName();

        StringBuffer jdbcConnString = new StringBuffer("jdbc:mysql://");

        jdbcConnString.append(host);
        if (StringUtils.isNotEmpty(port)) {
            jdbcConnString.append(":").append(port);
        }

        if (StringUtils.isNotEmpty(database)) {
            jdbcConnString.append("/").append(database);
        }

        return jdbcConnString.toString();
    }

    /**
     * retrieve 'selenium-hub url' from app configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getSeleniumHubURL() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.appConfigs);

        String hubUrl = this.appConfigs.getOrDefault(AppConfig.HUB_URL, StringUtils.EMPTY);

        if (StringUtils.isEmpty(hubUrl)) {
            LOG.warn("Selenium-Hub URL is not set.");
        }

        return hubUrl;
    }

    /**
     * retrieve 'browser' from app configs.
     *
     * @return
     * @throws NotInitializedException
     */
    public String getBrowserType() throws NotInitializedException {
        throwExceptionIfNotInitialized();

        assert Objects.nonNull(this.appConfigs);

        String browser = this.appConfigs.getOrDefault(AppConfig.BROWSER, StringUtils.EMPTY);

        if (StringUtils.isEmpty(browser)) {
            LOG.warn("'browser' is not set.");
        }

        return browser;
    }


    /**
     * initialize <code>ConfManager</code> instance.
     * This method, <code>init</code>, must be called before use.
     *
     * @param args
     * @return
     */
    synchronized public boolean init(String[] args) {
        if (Objects.isNull(args)) {
            return false;
        }

        try {
            //  initialize 'app' options
            this.appOptions = AppOption.parse(args);

            if (checkingAppOptions(this.appOptions)) {
                String confPath = this.appOptions.get(AppOption.CONF);

                if (StringUtils.isEmpty(confPath)) {
                    confPath = getDefaultConfigDirectory(this.appOptions.get(AppOption.HOME));
                }

                //  initialize 'db' configs.
                {
                    String dbConfigFilepath = String.format("%s%s%s", confPath, confPath.endsWith(File.separator) ? "" : File.separator, DatabaseConfigFilename);
                    this.dbConfigs = DBConfig.parse(dbConfigFilepath);
                }

                //  initialize 'app' configs
                {
                    String appConfigFilepath = String.format("%s%s%s", confPath, confPath.endsWith(File.separator) ? "" : File.separator, AppConfigFilename);
                    this.appConfigs = AppConfig.parse(appConfigFilepath);
                }
            }

            return true;
        }
        catch (Exception excp) {
            excp.printStackTrace();
        }

        clear();

        return false;
    }

    private boolean checkingAppOptions(Map<AppOption, String> options) {
        //  Log
        {
            for (Map.Entry<AppOption, String> option : options.entrySet()) {
                //  Log
                LOG.debug("{} => {}", option, option.getValue());

                AppOption currentOption = option.getKey();
                if (currentOption == AppOption.HOME ||
                        currentOption == AppOption.CONF ||
                        currentOption == AppOption.DONWLOAD) {
                    try {
                        Path path = Paths.get(option.getValue());
                        if (!Files.exists(path)) {
                            Files.createDirectories(path);
                        }
                    }
                    catch (IOException ioexcp) {
                        ioexcp.printStackTrace();

                        return false;
                    }
                }
            }
        }

        return true;
    }
}
