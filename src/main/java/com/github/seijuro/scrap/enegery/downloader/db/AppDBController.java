package com.github.seijuro.scrap.enegery.downloader.db;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import com.github.seijuro.scrap.enegery.downloader.db.record.DownloadHistoryRecord;

import java.sql.SQLException;
import java.util.Objects;

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

    /**
     *
     *
     * @return
     * @throws SQLException
     */
    public DownloadHistoryRecord getLatestElectrocityFile() throws SQLException {
        assert Objects.nonNull(this.conn);

        return DownloadHistoryHelper.DML.selectLatestElectrocityFileFromDownloadHistory(this.conn);
    }

    /**
     *
     *
     * @return
     * @throws SQLException
     */
    public DownloadHistoryRecord getLatestGasFile() throws SQLException {
        assert Objects.nonNull(this.conn);

        return DownloadHistoryHelper.DML.selectLatestGasFileFromDownloadHistory(this.conn);
    }

    public DownloadHistoryRecord getEnergyFileWhoseDateYMIs(EnergyType $type, int $dateYM) throws SQLException {
        assert Objects.nonNull(this.conn);

        return DownloadHistoryHelper.DML.selectEnergyFileWhoseTypeAndDateYMIs(this.conn, $type, $dateYM);
    }

    /**
     *
     *
     * @param $fileId
     * @param $type
     * @param dateYM
     * @param $path
     * @return
     * @throws SQLException
     */
    public int upsertEnergyFile(String $fileId, EnergyType $type, int dateYM, String $path) throws SQLException {
        assert Objects.nonNull(this.conn);

        return DownloadHistoryHelper.DML.upsertEnergyFile(this.conn, $fileId, $type, dateYM, $path);
    }

    /**
     *
     *
     * @param $fileId
     * @param $status
     * @return
     * @throws SQLException
     */
    public int updateEnergyFileStatus(String $fileId, int $status) throws SQLException {
        assert Objects.nonNull(this.conn);

        return DownloadHistoryHelper.DML.updateEnergyFileStatus(this.conn, $fileId, $status);
    }
}
