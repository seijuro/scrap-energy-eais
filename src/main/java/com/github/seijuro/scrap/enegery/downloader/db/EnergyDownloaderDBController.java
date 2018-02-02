package com.github.seijuro.scrap.enegery.downloader.db;

public class EnergyDownloaderDBController extends MySQLDBController {
    /**
     * create method to instantiate controller.
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     * @return
     */
    public static EnergyDownloaderDBController create(String $host, String $user, String $passwd, String $database) {
        return new EnergyDownloaderDBController($host, null, $user, $passwd, $database);
    }

    /**
     * C'tor
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     */
    protected EnergyDownloaderDBController(String $host, String $port, String $user, String $passwd, String $database) {
        super($host, $port, $user, $passwd, $database);
    }

}
