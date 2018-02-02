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
        FILE_TYPE("type"),
        DATE_YM("ym"),
        FILEPATH("filepath"),
        STATUS("status"),
        LAStUPDATE("lastupdate");

        @Getter
        private final String name;

        Column(String $name) {
            this.name = $name;
        }
    }
}
