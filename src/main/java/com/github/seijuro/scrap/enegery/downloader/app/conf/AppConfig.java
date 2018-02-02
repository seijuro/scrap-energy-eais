package com.github.seijuro.scrap.enegery.downloader.app.conf;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum AppConfig implements Config {
    HUB_URL("hub.url"),
    BROWSER("browser");

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
    AppConfig(String $name) {
        this.name = $name;
    }

    /**
     * Class Instance
     */
    private static Logger LOG = LoggerFactory.getLogger(AppConfig.class);
    private static String Tag = "[CONFIG/APP]";

    /**
     *
     * @param path
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static Map<Config, String> parse(String path) throws IllegalArgumentException, IOException {
        //  check param #1
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Parameter, path, is null.");
        }

        LOG.debug("{} path : {}", Tag, path);

        Path configPath = Paths.get(path);

        //  check param #2
        if (Files.exists(configPath) &&
                Files.isReadable(configPath)) {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            Map<Config, String> ret = new HashMap<>();

            while ((line = br.readLine()) != null) {
                Object[] parsed = ConfigLineParser.parse(line, AppConfig.values());

                if (Objects.nonNull(parsed) &&
                        parsed.length == 2) {
                    //  Log
                    LOG.debug("{} intput : [{}] ... {} => {}", Tag, line, parsed[0], parsed[1]);

                    ret.put(AppConfig.class.cast(parsed[0]), String.class.cast(parsed[1]));
                }
            }

            br.close();

            return ret;
        }

        throw new IllegalArgumentException(String.format("Param, path, is not valid (path : %s)", path.toString()));
    }
}
