package com.github.seijuro.scrap.enegery.downloader.app;

import lombok.Getter;
import lombok.NonNull;

public enum FileType {
    ZIP(new String[] {"zip"}),
    GZIP(new String[] {"gzip", "gz", "tgz"}),
    TXT(new String[] {"txt"}),
    XLS(new String[] {"xls"}),
    PDF(new String[] {"pdf"}),
    UNKNOWN(new String[] {});

    @NonNull
    @Getter
    private final String[] extensions;

    /**
     * C'tor
     *
     * @param set
     */
    FileType(String[] set) {
        this.extensions = set;
    }
}
