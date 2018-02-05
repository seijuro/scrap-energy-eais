package com.github.seijuro.scrap.enegery.downloader.db.schema;

import lombok.Getter;

public class DownloadHistoryTable {
    /**
     * Class Instance
     */
    @Getter
    private static final String Name = "EnergyDownloadHistory";

    public enum Column implements com.github.seijuro.scrap.enegery.downloader.db.Column {
        IDX("idx"),
        FILE_ID("fid"),
        TYPE("etype"),
        DATE_YM("ym"),
        FILEPATH("path"),
        STATUS("status"),
        LASTUPDATE("lastupdate");

        @Getter
        private final String name;

        Column(String $name) {
            this.name = $name;
        }
    }
}
