package com.github.seijuro.scrap.enegery.downloader.app.conf;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ConfigLineParser {
    /**
     * Class Properties
     */
    private static final String AssignementOpr = "=";
    private static final String CommentOpr = "#";

    static boolean isComment(String line) {
        return line.startsWith(CommentOpr);
    }

    static Object[] parse(String line, Config[] configs) {
        String trimed = StringUtils.stripToEmpty(line);

        if (!isComment(trimed) &&
                StringUtils.isNotEmpty(trimed)) {
            String[] tokens = trimed.split(AssignementOpr, 2);

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
