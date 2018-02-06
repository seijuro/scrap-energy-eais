package com.github.seijuro.scrap.enegery.downloader.task;

import com.github.seijuro.common.http.HttpRequestHelper;
import com.github.seijuro.common.http.RequestMethod;
import com.github.seijuro.common.util.ZipUtils;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyFileEvent;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyFileEventSubscriber;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import com.github.seijuro.scrap.enegery.downloader.app.conf.ConfigManager;
import com.github.seijuro.scrap.enegery.downloader.app.conf.NotInitializedException;
import com.github.seijuro.scrap.enegery.downloader.db.ctrl.AppDBController;
import com.github.seijuro.scrap.enegery.downloader.db.record.DownloadHistoryRecord;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Objects;

public class EnergyFileSubscribingTask extends EnergyFileEventSubscriber implements Runnable {
    /**
     * Class Instance(s)
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyFileSubscribingTask.class);
    private static final String Tag = "[SUBSCRIBER][ENERGY]";

    private static final String EnergyFileGroupCode = "100";
    private static final String EnergyFileGroupDetailCode = "101";
    private static final String ZipFileCharsetName = "CP949";

    private static final String RequestURL = "http://open.eais.go.kr/opnsvc/opnOpenInfoDownload.do";
    private static final String Referer = "http://open.eais.go.kr/opnsvc/opnSvcOpenInfoView.do";

    private static final long DefaultThreadSleepMillis = 120L * DateUtils.MILLIS_PER_SECOND;


    private static final Charset getZipFileCharset() {
        return Charset.forName(ZipFileCharsetName);
    }


    /**
     * Request Post Parameter(s)
     */
    private enum PostParameters {
        GROUP_CODE("pubDataGrpCd"),
        GROUP_DETAIL_CODE("pubDataDetlGrpCd"),
        ARTIFACT_ID("atfiGbId");

        /**
         * Instance Properties
         */
        @Getter
        private final String name;

        /**
         * C'tor
         *
         * @param $name
         */
        PostParameters(String $name) {
            this.name = $name;
        }
    }

    /**
     * generate Request(POST) parameter string.
     *
     * @param group
     * @param detail
     * @param artifactId
     * @return
     */
    private static String genPostParameter(String group, String detail, String artifactId) {
        return String.format("%s=%s&%s=%s&%s=%s",
                PostParameters.GROUP_CODE.getName(), group,
                PostParameters.GROUP_DETAIL_CODE.getName(), detail,
                PostParameters.ARTIFACT_ID.getName(), artifactId);
    }

    /**
     * generate Request(POST) parameter string.
     * Group code & group detail code for energy data is already defined.
     * Above all, to scrap group code & group detail, you must go through 'login' procedure.
     *
     * @param $fileId
     * @return
     */
    private static String genEnergyFilePostParameter(String $fileId) {
        return genPostParameter(EnergyFileGroupCode, EnergyFileGroupDetailCode, $fileId);
    }

    /**
     * retreive filename from disposition
     *
     * @param disposition
     * @return
     */
    private static String retreiveFilenameFromDisposition(String disposition) {
        try {
            if (Objects.nonNull(disposition)) {
                //  extracts filename from header field
                String[] tokens = disposition.split(";");

                for (String token : tokens) {
                    String normalized = StringUtils.stripToEmpty(token);

                    if (normalized.startsWith("filename")) {
                        return StringUtils.stripToEmpty(URLDecoder.decode(normalized.substring(10, normalized.length() - 1), "UTF-8"));
                    }
                }
            }
        }
        catch (UnsupportedEncodingException ueexcp) {
            ueexcp.printStackTrace();
        }

        return StringUtils.EMPTY;
    }

    /**
     * Instance Properties
     */
    @Getter(AccessLevel.PROTECTED)
    private final String downloadDirectory;
    @Getter(AccessLevel.PROTECTED)
    private final String unzipDirectory;
    @Setter
    private long looopSleepMillis = DefaultThreadSleepMillis;

    /**
     * C'tor
     *
     * @param downDir
     * @param unzipDir
     */
    public EnergyFileSubscribingTask(String downDir, String unzipDir) throws IllegalArgumentException {
        assert StringUtils.isNotEmpty(downDir);
        assert StringUtils.isNotEmpty(unzipDir);

        this.downloadDirectory = downDir.endsWith(File.separator) ? downDir : String.format("%s%s", downDir, File.separator);
        this.unzipDirectory = unzipDir.endsWith(File.separator) ? unzipDir : String.format("%s%s", unzipDir, File.separator);
    }

    /**
     *
     * @param ctrl
     * @param event
     * @return
     */
    private DownloadHistoryRecord.Status getDownloadHistoryStatus(AppDBController ctrl, EnergyFileEvent event) {
        DownloadHistoryRecord record = getDownloadHistory(ctrl, event);

        if (Objects.nonNull(record)) {
            return record.getStatus();
        }

        return null;
    }

    private DownloadHistoryRecord getDownloadHistory(AppDBController ctrl, EnergyFileEvent event) {
        assert Objects.nonNull(ctrl) && Objects.nonNull(event);

        int dateYM = event.getDateYM();

        try {
            DownloadHistoryRecord record = ctrl.getEnergyFileWhoseDateYMIs(event.getType(), dateYM);

            if (Objects.nonNull(record)) {
                DownloadHistoryRecord.Status status = record.getStatus();

                return record;
            }
        }
        catch (SQLException sqlexcp) {
            sqlexcp.printStackTrace();
        }

        return null;
    }

    /**
     * If the energy type of file is one of 'electrocity' and 'gas', return true. Otherwise, return false.
     *
     * @param type
     * @return
     */
    private boolean checkEnergyType(EnergyType type) {
        if (Objects.nonNull(type)) {
            if (EnergyType.ELECTROCITY == type ||
                    EnergyType.GAS == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * download file that event describes.
     *
     * @param ctrl
     * @param event
     * @return
     */
    private boolean downloadEnergyFile(AppDBController ctrl, EnergyFileEvent event) {
        assert Objects.nonNull(ctrl) && Objects.nonNull(event);

        try {
            String fileId = event.getFileId();
            EnergyType type = event.getType();
            int dateYM = event.getYear() * 100 + event.getMonth();

            URL url = new URL(RequestURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            //  set method & properties
            {
                HttpRequestHelper.setRequestMethod(httpConn, RequestMethod.POST);
                HttpRequestHelper.setReferer(httpConn, Referer);
                HttpRequestHelper.setUserAgent(httpConn);
            }

            //  set parameter
            {
                String postParam = genEnergyFilePostParameter(fileId);

                //  Log
                LOG.debug("{} POST param : {}", Tag, postParam);

                httpConn.setDoOutput(true);
                DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                dos.writeBytes(postParam);
                dos.flush();
                dos.close();
            }

            int responseCode = httpConn.getResponseCode();

            //  Log
            LOG.debug("{} response code : {}", Tag, responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                //  TODO
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                String fileExt = event.getFileType().getExtensions()[0];
                String fileYM = String.format("%d%02d", event.getYear(), event.getMonth());
                String filename = retreiveFilenameFromDisposition(disposition);

                //  Log(s)
                LOG.debug("{} Content-Type : {}", Tag, contentType);
                LOG.debug("{} Content-Disposition : {}", Tag, disposition);
                LOG.debug("{} Content-Length : {}", Tag, contentLength);

                if (StringUtils.isNotEmpty(filename) &&
                        filename.endsWith(fileExt) &&
                        !filename.contains(fileYM)) {
                    filename = String.format("%s_%04d%02d.%s", event.getType(), event.getYear(), event.getMonth(), event.getFileType().getExtensions()[0]);
                }

                try {
                    String destFilepath = getDownloadDirectory() + filename;
                    //  Log
                    LOG.debug("{} filepath : {}", destFilepath);

                    int affectedRows = ctrl.upsertEnergyFile(fileId, event.getType(), dateYM, destFilepath);
                    LOG.debug("affected Rows : {}", affectedRows);

                    if (affectedRows > 0) {
                        try {
                            InputStream is = httpConn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(destFilepath);

                            final int BufferSize = 4096;
                            int bytesRead;
                            byte[] buffer = new byte[BufferSize];

                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }

                            fos.close();
                            is.close();

                            //  Log
                            LOG.debug("{} Downloading file is done ... (at : {})", Tag, destFilepath);

                            affectedRows = ctrl.updateEnergyFileStatus(fileId, DownloadHistoryRecord.Status.DOWNLOADED.getCode());

                            //  Log
                            LOG.debug("{} Update 'status' of downloading history from 'INIT' to 'DONE' ... (affected rows : {})", Tag, affectedRows);

                            ctrl.commit();

                            //  Log
                            LOG.debug("{} Commit changes ...", Tag);
                        }
                        catch (IOException ioexcp) {
                            ioexcp.printStackTrace();

                            //  Log
                            LOG.warn("{} Rollback changes ...", Tag);
                            ctrl.rollback();

                            throw ioexcp;
                        }
                    }

                    return true;
                }
                catch (SQLException sqlexcp) {
                    sqlexcp.printStackTrace();
                }
            }
            else {
                //  TODO handle http error
                LOG.error("{} HTTP ERROR ...", Tag);
            }
        }
        catch (MalformedURLException mfuexcp) {
            mfuexcp.printStackTrace();
        }
        catch (IOException ioexcp) {
            ioexcp.printStackTrace();
        }

        return false;
    }

    private boolean unzipEnergyFile(AppDBController ctrl, String src, String outDir) {
        assert Objects.nonNull(ctrl) && Objects.nonNull(src) && Objects.nonNull(outDir);

        LOG.debug("src : {}", src);
        LOG.debug("out directory : {}", outDir);

        Path srcPath = Paths.get(src);
        Path outPath = Paths.get(outDir);

        ZipUtils.unzip(srcPath, outPath, getZipFileCharset());

        return true;
    }

    /**
     * handle event.
     *
     * @param event
     */
    private void handleEvent(AppDBController ctrl, EnergyFileEvent event) {
        if (Objects.nonNull(event) &&
                Objects.nonNull(event.getType())) {
            //  Log
            LOG.debug("{}, handle event ... (event : {})", Tag, event.toString());

            EnergyType type = event.getType();

            //  check event #1
            if (!checkEnergyType(type)) {
                //  Log
                LOG.warn("{} SKIP ... Unknown type ({})", Tag, type);

                return;
            }

            DownloadHistoryRecord record = getDownloadHistory(ctrl, event);
            DownloadHistoryRecord.Status status = Objects.nonNull(record) ? record.getStatus() : null;

            //  Log
            LOG.debug("Chechking downloading ... ");

            if (Objects.isNull(status) ||
                    status.lessThan(DownloadHistoryRecord.Status.DOWNLOADING)) {
                //  Step #1. Download
                if (downloadEnergyFile(ctrl, event)) {
                    //  Log
                    LOG.debug("{} Downloading energy file is done ... (event : {})", Tag, event);

                    record = getDownloadHistory(ctrl, event);
                    status = Objects.nonNull(record) ? record.getStatus() : null;

                    //  Log
                    LOG.debug("{} Create(or update) download history ... (history : {})", Tag, record);
                }
                else {
                    //  Log
                    LOG.error("{} Downloading file for event ({}) failed.", Tag, event);

                    return;
                }
            }

            //  Log
            LOG.debug("Chechking unzipping ... ");

            if (status.lessThan(DownloadHistoryRecord.Status.DONE)) {
                Path path = Paths.get(record.getFilepath());

                if (Files.exists(path) &&
                        Files.isReadable(path)) {

                    if (ZipUtils.isZipFile(path)) {
                        try {
                            //  Unzip
                            String outDirectory = String.format("%s%d%s", getUnzipDirectory(), event.getDateYM(), File.separator);

                            DownloadHistoryRecord.Status finalStatus;
                            if (unzipEnergyFile(ctrl, record.getFilepath(), outDirectory)) {
                                //  Log
                                LOG.debug("Unzipping energy file(s) is done successfully ... (filepath : {})", record.getFilepath());

                                finalStatus = DownloadHistoryRecord.Status.DONE;
                            }
                            else {
                                //  Log
                                LOG.warn("Unzipping energy file(s) failed ... (filepath : {})", record.getFilepath());

                                finalStatus = DownloadHistoryRecord.Status.ERROR;
                            }

                            ctrl.updateEnergyFileStatus(record.getFileId(), finalStatus.getCode());
                            ctrl.commit();
                        }
                        catch (SQLException sqlexcp) {
                            sqlexcp.printStackTrace();
                        }
                    }
                    else {
                        //  Log
                        LOG.warn("File isn't a zip file ... (filepath : {})", record.getFilepath());
                    }
                }
                else {
                    //  Log
                    LOG.warn("File doens't exists or not readable ... (filepath : {})", record.getFilepath());
                }
            }
            else if (status == DownloadHistoryRecord.Status.DONE) {
                //  Log
                LOG.debug("{} Downlaoding & Unzipping is already done ... (event : {})", Tag, event);
            }
        }
    }

    /**
     * Implement <code>Runnable</code> interface.
     */
    @Override
    public void run() {
        try {
            do {
                ConfigManager confManager = ConfigManager.getInstance();
                String dbHost = confManager.getDatabaseHost();
                String dbUser = confManager.getDatabaseUser();
                String dbPasswd = confManager.getDatabasePassword();
                String dbName = confManager.getDatabaseName();

                AppDBController appDBController = AppDBController.create(dbHost, dbUser, dbPasswd, dbName);

                try {
                    appDBController.connect();
                    appDBController.setAutoCommit(false);

                    while (countEvents() > 0) {
                        EnergyFileEvent event = pollEvent();

                        //  Log
                        LOG.debug("{} loop ... event : {}", Tag, event.toString());

                        handleEvent(appDBController, event);
                    }

                    appDBController.close();
                }
                catch (SQLException sqlexcp) {
                    sqlexcp.printStackTrace();
                }

                //  Log
                LOG.debug("{} loop ... sleep : {}", Tag, this.looopSleepMillis);

                Thread.sleep(this.looopSleepMillis);
            } while (true);
        }
        catch (NotInitializedException niexcp) {
            niexcp.printStackTrace();
        }
        catch (InterruptedException excp) {
            excp.printStackTrace();
        }
    }
}
