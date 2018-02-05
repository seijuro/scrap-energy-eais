package com.github.seijuro.scrap.energy.downloader.db;

import com.github.seijuro.scrap.enegery.downloader.db.ctrl.AppDBController;
import com.github.seijuro.scrap.enegery.downloader.db.record.DownloadHistoryRecord;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AppDBControllerTest {

    static final Logger LOG = LoggerFactory.getLogger(AppDBControllerTest.class);

    @Test
    public void testQuery() {
        AppDBController appDbCtrl = AppDBController.create("albatross", "kds", "kds0809", "Dev");

        try {
            appDbCtrl.connect();

            DownloadHistoryRecord latestElec = appDbCtrl.getLatestElectrocityFile();
            DownloadHistoryRecord latestGas = appDbCtrl.getLatestGasFile();

            LOG.debug("[ENERGY/ELEC] : {}", latestElec);
            LOG.debug("[ENERGY/GAS] : {}", latestGas);

            appDbCtrl.close();

            //  assert
            Assert.assertTrue(true);

            return;
        }
        catch (SQLException excp) {
            excp.printStackTrace();
        }

        //  assert
        Assert.assertTrue(false);
    }
}
