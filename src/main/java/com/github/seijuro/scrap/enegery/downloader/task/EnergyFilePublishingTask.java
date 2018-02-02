package com.github.seijuro.scrap.enegery.downloader.task;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyFileEvent;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyFilePublisher;
import com.github.seijuro.scrap.enegery.downloader.app.FileType;
import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnergyFilePublishingTask extends Thread {
    /**
     * Class Instance(s)
     */
    public static Logger LOG = LoggerFactory.getLogger(EnergyFilePublishingTask.class);

    public static String[] TabKeywords = {"지번별", "에너지", "사용량"};
    public static final String Tag = "[THREAD/CHECKER#NEW]";
    private static Pattern DatePattern = Pattern.compile("[0-9]+\\.[0-9]+");

    public static final long DefaultLoopSleepMillis = 10 * DateUtils.MILLIS_PER_MINUTE;
    public static final long DefaultClickSleepMillis = 3 * DateUtils.MILLIS_PER_SECOND;

    /**
     * list item consist 2 elements (normal element, expanded element).
     * The normal element's class is 'a'.
     * The expaneded element's class is 'p'.
     * <code>ListItemSpanType</code> is designed to represent theses elements.
     */
    private enum ListItemSpanType {
        UNKNOWN(StringUtils.EMPTY),
        NORMAL("p"),
        APPEND("a");

        /**
         * Instance properties
         */
        @NonNull
        @Getter
        private final String className;

        /**
         * C'tor
         *
         * @param $name
         */
        ListItemSpanType(String $name) {
            this.className = $name;
        }
    }

    /**
     * Normal list item consist of 3 elements.
     * Each element contains informations(meta, title, hits).
     * <code>NormalListItemSpanType</code> is designed to represent these elements.
     */
    private enum NormalListItemSpanType {
        META("left"),
        TITLE(StringUtils.EMPTY),
        HITS("scnt"),
        UNKNOWN(StringUtils.EMPTY);

        @NonNull
        @Getter
        private final String className;

        /**
         * C'tor
         *
         * @param name
         */
        NormalListItemSpanType(String name) {
            this.className = name;
        }

        private static class Helper {
            public static NormalListItemSpanType getType(WebElement element) {
                if (Objects.nonNull(element)) {
                    String className = StringUtils.stripToEmpty(element.getAttribute("class"));

                    for (NormalListItemSpanType type : NormalListItemSpanType.values()) {
                        if (type.getClassName().equalsIgnoreCase(className)) {
                            return type;
                        }
                    }
                }

                return NormalListItemSpanType.UNKNOWN;
            }

            /**
             * retrieve 'id' from the given element that contains 'meta' information.
             * Of course, this must check whether type of given element is 'meta' or not.
             * This time, instead of checking the type of elements and throw exception, this would return null if it wasn't valid.
             *
             * @param
             * @return
             */
            public static String getId(WebElement element ) {
                assert Objects.nonNull(element);

                //  check if element's type is equals to 'META'
                //  ... SKIP

                return StringUtils.stripToEmpty(element.getAttribute("data-ntcgbid"));
            }

            /**
             * retrieve 'id' from the given element that contains 'title' information.
             * Of course, this must check whether type of given element is 'title' or not.
             * This time, instead of checking the type of elements and throw exception, this would return null if it wasn't valid.
             *
             * @param
             * @return
             */
            public static String getTitle(WebElement element) {
                assert Objects.nonNull(element);

                //  check if element's type is equals to 'TITLE'
                //  ... SKIP

                return StringUtils.stripToEmpty(element.getText());
            }

            /**
             * retrieve 'id' from the given element that contains 'hits' information.
             * Of course, this must check whether type of given element is 'hits' or not.
             * This time, instead of checking the type of elements and throw exception, this would return null if it wasn't valid.
             *
             * @param
             * @return
             */
            public static String getHits(WebElement element) {
                assert Objects.nonNull(element);

                //  check if element's type is equals to 'HITS'
                //  ... SKIP

                return StringUtils.stripToEmpty(element.getText());
            }
        }
    }

    /**
     * Each list item has link element to download file.
     * The type of hese file is one of 'TXT', 'XLS', 'PDF', 'ZIP'.`
     * <code>FileTypeImage</code> is designed to define them.
     */
    private enum FileTypeImage {
        TXT("down_img_txt", FileType.TXT),
        XLS("down_img_xlsx", FileType.XLS),
        PDF("down_img_pdf", FileType.PDF),
        ZIP("down_img_zip", FileType.ZIP);

        @NonNull
        @Getter
        private final String name;
        @NonNull
        @Getter
        private final FileType type;

        FileTypeImage(String $name, FileType $type) {
            this.name = $name;
            this.type = $type;
        }
    }

    /**
     * Retrieving the first date text from title.
     *
     * For example, this method return '2017.01' for the input text, "hello, guys!  - 2017.01 -".
     *
     * @param text
     * @param index
     * @return
     */
    protected static String retrieveDateFromTitle(String text, int index) {
        Matcher matcher = DatePattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(index);
        }

        return StringUtils.EMPTY;
    }


    private static FileType getFileType(WebElement element) {
        if (Objects.nonNull(element)) {
            String className = StringUtils.stripToEmpty(element.getAttribute("class"));

            for (FileTypeImage ftimg : FileTypeImage.values()) {
                if (ftimg.getName().equalsIgnoreCase(className)) {
                    return ftimg.getType();
                }
            }
        }

        return FileType.UNKNOWN;

    }

    private static ListItemSpanType getItemSectionType(WebElement element) {
        if (Objects.nonNull(element)) {
            String className = element.getAttribute("class");

            for (ListItemSpanType type : ListItemSpanType.values()) {
                if (type.getClassName().equalsIgnoreCase(className)) {
                    return type;
                }
            }
        }

        return ListItemSpanType.UNKNOWN;
    }

    /**
     * Instance Properties
     */
    @Getter
    private final URL requestURL;
    @Getter
    private final URL hubURL;
    @Getter
    private final String browser;
    private WebDriver webDriver = null;
    private boolean isRunning = true;
    @Setter @Getter
    private long loopSleepMillis = DefaultLoopSleepMillis;
    @Setter @Getter
    private long clickSleepMillis = DefaultClickSleepMillis;

    protected boolean isRunning() {
        return this.isRunning;
    }

    protected WebDriver openWebDriver() throws InterruptedException {
        if (Objects.isNull(this.webDriver)) {
            ChromeOptions opts = new ChromeOptions();
            opts.addArguments("--incognito");

            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, opts);

            this.webDriver = new RemoteWebDriver(getHubURL(), capabilities);

            Thread.sleep(getClickSleepMillis());
        }

        return this.webDriver;
    }

    protected void closeWebDriver() {
        if (Objects.isNull(this.webDriver)) {
            return;
        }

        this.webDriver.close();
        this.webDriver = null;
    }

    /**
     * C'tor
     *
     * @param $hubURL
     * @param $browser
     * @param $requestURL
     * @throws IllegalArgumentException
     * @throws MalformedURLException
     */
    public EnergyFilePublishingTask(
            String $hubURL,
            String $browser,
            String $requestURL)
            throws IllegalArgumentException, MalformedURLException {
        if (StringUtils.isEmpty($hubURL) ||
                StringUtils.isEmpty($requestURL)) {
            throw new IllegalArgumentException(String.format("Parameters, url(s), is an empty string ... (hubURL : %s, browser : %s, requestURL : %s)", $hubURL, $browser, $requestURL));
        }

        this.hubURL = new URL($hubURL);
        this.browser = $browser;
        this.requestURL = new URL($requestURL);
    }

    public boolean isEnergyTab(WebElement element) {
        return isEnergyTab(element.getText());
    }

    public boolean isEnergyTab(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        boolean result = true;

        for (String keyword : TabKeywords) {
            if (!text.contains(keyword)) {
                result = false;

                break;
            }
        }

        return result;
    }

    protected boolean checkNewFileUploaded() throws InterruptedException {
        WebDriver webDriver = openWebDriver();
        List<WebElement> tabElements = webDriver.findElements(By.cssSelector("div.address_tab ul.tabbox_list li a"));

        //  Log
        LOG.debug("{} # of tab : {}", Tag, tabElements.size());

        int index = 0;
        WebElement energyTabElement = null;

        for (WebElement element : tabElements) {
            String tabText = element.getText();

            //  Log
            LOG.debug("{} {} : {}", Tag, index++, element.getText());

            if (StringUtils.isEmpty(tabText)) {
                continue;
            }

            if (isEnergyTab(element)) {
                energyTabElement = element;

                break;
            }
        }

        //  If found 'energy' tab
        if (Objects.nonNull(energyTabElement)) {
            energyTabElement.click();

            Thread.sleep(DefaultClickSleepMillis);

            List<WebElement> itemElements = webDriver.findElements(By.cssSelector("div#pvd_list div.pvd_wrap"));

            //  Log
            LOG.debug("{} # of item : {}", Tag, itemElements.size());

            index = 0;
            for (WebElement element : itemElements) {

                //  Log
                LOG.debug("{} {} : {}", Tag, index++, element.getText());

                EnergyFileEvent.Builder energyFileBuilder = new EnergyFileEvent.Builder();

                WebElement basicElement = element.findElement(By.cssSelector("div.q"));
                List<WebElement> spanElements = basicElement.findElements(By.cssSelector("span"));

                for (WebElement spanElement : spanElements) {
                    NormalListItemSpanType type = NormalListItemSpanType.Helper.getType(spanElement);

                    //  Log
                    LOG.debug("{} text : {}", Tag, spanElement.getText());

                    if (type == NormalListItemSpanType.META) {
                        String fid = NormalListItemSpanType.Helper.getId(spanElement);
                        energyFileBuilder.setFileId(fid);

                        //  Log
                        LOG.debug("{} id : {}", Tag, fid);
                    }
                    else if (type == NormalListItemSpanType.TITLE) {
                        String title = NormalListItemSpanType.Helper.getTitle(spanElement);
                        String dateText = retrieveDateFromTitle(title, 0);
                        String[] dateTokens = dateText.split("\\.");
                        EnergyType energyType = EnergyType.toEnergyTypeIfPossible(title);

                        //  Log
                        LOG.debug("{} title : {}, type : {}, date : {}", Tag, title, energyType, dateText);

                        assert dateTokens.length == 2;

                        String year = dateTokens[0];
                        String month = dateTokens[1];

                        energyFileBuilder.setType(energyType);
                        energyFileBuilder.setYear(year);
                        energyFileBuilder.setMonth(month);
                    }
                    else if (type == NormalListItemSpanType.HITS) {
                        //  Log
                        LOG.debug("{} hits : {}", Tag, NormalListItemSpanType.Helper.getHits(spanElement));
                    }
                }

                List<WebElement> fileLinkElements = element.findElements(By.cssSelector("div.a a"));

                int fidx = 1;

                EnergyFilePublisher publisher = EnergyFilePublisher.getInstance();

                for (WebElement fileLinkElement : fileLinkElements) {
                    FileType fileType = getFileType(fileLinkElement);
                    energyFileBuilder.setFileType(fileType);
                    energyFileBuilder.setFileSubId(fidx);

                    EnergyFileEvent fileInfo = energyFileBuilder.build();
                    publisher.notifyObservers(fileInfo);

                    //  Log
                    LOG.debug("{} File info. : {}", Tag, fileInfo.toString());
                }
            }
        }

        return true;
    }

    @Override
    public void run(){
        try {
            do {
                try {
                    WebDriver webDriver = openWebDriver();
                    webDriver.navigate().to(getRequestURL());

                    if (checkNewFileUploaded()) {
                        //  Log
                        LOG.debug("{} Found new file(s) uploaded ... ", Tag);
                    }
                }
                catch (InterruptedException iexcp) {
                    throw iexcp;
                }
                catch (Exception toexcp) {
                    toexcp.printStackTrace();
                }

                closeWebDriver();

                Thread.sleep(getLoopSleepMillis());
            } while (isRunning());

        }
        catch (InterruptedException excp) {
            this.isRunning = false;
        }

        closeWebDriver();
    }
}
