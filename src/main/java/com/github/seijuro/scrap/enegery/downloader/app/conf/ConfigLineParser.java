package com.github.seijuro.scrap.enegery.downloader.app.conf;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ConfigLineParser {
    private static final String AssignementOpr = "=";

    static Object[] parse(String line, Config[] configs) {
        if (Objects.nonNull(line)) {
            String[] tokens = line.split(AssignementOpr, 2);

            if (tokens.length == 2) {
                String key = StringUtils.strip(tokens[0]);
                String value = StringUtils.strip(tokens[1]);

                for (Config conf : configs) {
                    if (conf.getName().equalsIgnoreCase(key)) {
                        return new Object[] {conf, value};
                    }
                }
            }
        }

        return null;
    }
}
