package com.github.seijuro.scrap.enegery.downloader.task;

import com.github.seijuro.common.http.HttpRequestHelper;
import com.github.seijuro.common.http.RequestMethod;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyFileEvent;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyFileEventSubscriber;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class EnergyFileSubscribingTask extends EnergyFileEventSubscriber implements Runnable {
    /**
     * Class Instance(s)
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyFileSubscribingTask.class);
    private static final String Tag = "[SUBSCRIBER][ENERGY]";

    private static final String EnergyFileGroupCode = "100";
    private static final String EnergyFileGroupDetailCode = "101";

    private static final String RequestURL = "http://open.eais.go.kr/opnsvc/opnOpenInfoDownload.do";
    private static final String Referer = "http://open.eais.go.kr/opnsvc/opnSvcOpenInfoView.do";

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
     * @param fileId
     * @param subId
     * @return
     */
    private static String genEnergyFilePostParameter(String fileId, String subId) {
        return genPostParameter(EnergyFileGroupCode, EnergyFileGroupDetailCode, String.format("%s%s", fileId, subId));
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
     * handle event.
     *
     * @param event
     */
    private void handleEvent(EnergyFileEvent event) {
        if (Objects.nonNull(event) &&
                Objects.nonNull(event.getType())) {
            //  TODO check whether already download file mentionsed in event or not.

            if ((event.getYear() * 100 + event.getMonth()) < 201710) {
                return;
            }

            //  Log
            LOG.debug("{}, handle event ... (event : {})", Tag, event.toString());

            try {
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
                    String postParam = genEnergyFilePostParameter(event.getFileId(), event.getFileSubId());

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
                    LOG.debug("{} [[File Description]]\nContent-Type : {}\nContent-Disposition : {}\nContent-Length : {}\nfilename : {}", Tag, contentType, disposition, contentLength, filename);

                    if (StringUtils.isNotEmpty(filename) &&
                            filename.endsWith(fileExt) &&
                            !filename.contains(fileYM)) {
                        filename = String.format("%s_%04d%02d.%s", event.getType(), event.getYear(), event.getMonth(), event.getFileType().getExtensions()[0]);
                    }

                    InputStream is = httpConn.getInputStream();
                    String destFilepath = "/Users/myungjoonlee/Desktop/" + filename;

                    FileOutputStream fos = new FileOutputStream(destFilepath);

                    final int BufferSize = 4096;
                    int bytesRead = -1;
                    byte[] buffer = new byte[BufferSize];

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    fos.close();
                    is.close();

                    //  Log
                    LOG.debug("{} Downloading file is done ... (at : {})", Tag, destFilepath);
                }
                else {
                    //  TODO handle http error
                }
            }
            catch (MalformedURLException mfexcp) {
                mfexcp.printStackTrace();
            }
            catch (ProtocolException pexcp) {
                pexcp.printStackTrace();
            }
            catch (IOException ioexcp) {
                ioexcp.printStackTrace();
            }
        }
    }

    /**
     * Implement <code>Thead</code> interface.
     */
    @Override
    public void run() {
        try {
            do {
                while (countEvents() > 0) {
                    EnergyFileEvent event = pollEvent();

                    //  Log
                    LOG.debug("{} loop ... event : {}", Tag, event.toString());

                    handleEvent(event);
                }

                //  Log
                LOG.debug("{} loop ... sleep : {}", Tag, this.threadSleepMillis);

                Thread.sleep(this.threadSleepMillis);
            } while (true);

        }
        catch (InterruptedException excp) {
            excp.printStackTrace();
        }
    }
}
