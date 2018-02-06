package com.github.seijuro.scrap.enegery.downloader.app.option;

import lombok.Getter;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum AppOption {
    HOME("h", "home", "(Required) home directory.", true),
    CONF("c", "conf", "conf direcotry.", false);

    /**
     * Instance Properties
     */
    @Getter
    private final String shortName;
    @Getter
    private final String longName;
    @Getter
    private final String description;
    @Getter
    private final boolean required;

    /**
     * C'tor
     *
     * @param $shortNamme
     * @param $longName
     */
    AppOption(String $shortNamme, String $longName, String $desc, boolean $required) {
        this.shortName = $shortNamme;
        this.longName = $longName;
        this.description = $desc;
        this.required = $required;
    }

    /**
     * parse app arguments & retrieve option values from them.
     *
     * @param args
     * @return
     */
    public static Map<AppOption, String> parse(String[] args) throws ParseException, NullPointerException{
        if (Objects.nonNull(args)) {
            Map<AppOption, String> ret = new HashMap<>();

            Option optionHome = Option.builder(AppOption.HOME.getShortName())
                    .required(AppOption.HOME.isRequired())
                    .hasArg(true)
                    .numberOfArgs(1)
                    .longOpt(AppOption.HOME.getLongName())
                    .desc(AppOption.HOME.getDescription())
                    .build();
            Option optionConf = Option.builder(AppOption.CONF.getShortName())
                    .required(AppOption.CONF.isRequired())
                    .hasArg(true)
                    .numberOfArgs(1)
                    .longOpt(AppOption.CONF.getLongName())
                    .desc(AppOption.CONF.getDescription())
                    .build();

            Options options = new Options();
            options.addOption(optionHome);
            options.addOption(optionConf);

            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            String home = commandLine.getOptionValue(AppOption.HOME.getShortName());

            if (commandLine.hasOption(AppOption.HOME.getShortName())) {
                ret.put(AppOption.HOME, home);
            }

            if (commandLine.hasOption(AppOption.CONF.getShortName())) {
                ret.put(AppOption.CONF, commandLine.getOptionValue(AppOption.CONF.getShortName()));
            }

            return ret;
        }

        throw new NullPointerException();
    }
}
