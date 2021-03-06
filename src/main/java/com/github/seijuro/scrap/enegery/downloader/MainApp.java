package com.github.seijuro.scrap.enegery.downloader;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyFilePublisher;
import com.github.seijuro.scrap.enegery.downloader.app.conf.ConfigManager;
import com.github.seijuro.scrap.enegery.downloader.task.EnergyFilePublishingTask;
import com.github.seijuro.scrap.enegery.downloader.task.EnergyFileSubscribingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainApp {
    /**
     * Class Instance
     */
    private static Logger LOG = LoggerFactory.getLogger(MainApp.class);
    private static final String EnergyFileSiteURL = "http://open.eais.go.kr/opnsvc/opnSvcOpenInfoView.do";

    public static void main(String[] args) {
        try {
            ConfigManager confManager = ConfigManager.getInstance();
            if (!confManager.init(args)) {
                LOG.error("Initializing 'ConfManager' failed ...");

                return;
            }

            String seleniumServerURL = confManager.getSeleniumHubURL();
            String browser = confManager.getBrowserType();
            String downDirectory = confManager.getDownloadDirecotry();
            String unzipDirectory = confManager.getUnzipDirecotry();
            long publishingMillis = confManager.getPublishingMillis();
            long subscribingMillis = confManager.getSubscribingMillis();

            //  Log(s)
            LOG.debug("publishing sleep millis : {}", publishingMillis);
            LOG.debug("subscribing sleep millis : {}", subscribingMillis);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            EnergyFilePublishingTask publisher = new EnergyFilePublishingTask(seleniumServerURL, browser, EnergyFileSiteURL);
            EnergyFileSubscribingTask subscriber = new EnergyFileSubscribingTask(downDirectory, unzipDirectory);

            publisher.setLoopSleepMillis(publishingMillis);
            subscriber.setLooopSleepMillis(subscribingMillis);

            EnergyFilePublisher.getInstance().add(subscriber);

            //  execute & join thread(s)
            {
                List<CompletableFuture<?>> futures = new ArrayList<>();

                futures.add(CompletableFuture.runAsync(publisher, executor));
                futures.add(CompletableFuture.runAsync(subscriber, executor));

                for (CompletableFuture<?> future : futures) {
                    future.join();
                }
            }

            //Log
            LOG.debug("Shutting down thread executor ...");

            executor.shutdown();
        }
        catch (Exception excp) {
            excp.printStackTrace();
        }
    }
}
