package com.github.seijuro.scrap.enegery.downloader.db;

public class AppDBController extends MySQLDBController {
    /**
     * create method to instantiate controller.
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     * @return
     */
    public static AppDBController create(String $host, String $user, String $passwd, String $database) {
        return new AppDBController($host, null, $user, $passwd, $database);
    }

    /**
     * C'tor
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     */
    protected AppDBController(String $host, String $port, String $user, String $passwd, String $database) {
        super($host, $port, $user, $passwd, $database);
    }

}
