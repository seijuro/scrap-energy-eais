package com.github.seijuro.scrap.enegery.downloader.app;

import com.github.seijuro.scrap.enegery.downloader.util.Notification;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnergyFileEvent implements <code>Notification</code>.
 *
 * - Immutable
 * - Supporting <code>Builder</code>
 */
@EqualsAndHashCode
@ToString
public class EnergyFileEvent implements Notification {
    private static Logger LOG = LoggerFactory.getLogger(EnergyFileEvent.class);

    /**
     * Instance Properties
     */
    @Getter
    private final int year;
    @Getter
    private final int month;
    @Getter
    private final EnergyType type;
    @Getter
    private final String fileMajorId;
    @Getter
    private final String fileMinorId;
    @Getter
    private final FileType fileType;

    public String getFileId() {
        return String.format("%s%s", this.fileMajorId, this.fileMinorId);
    }

    public int getDateYM() {
        return year * 100 + month;
    }

    /**
     * C'tor (Builder)
     *
     * @param builder
     */
    public EnergyFileEvent(Builder builder) {
        this.year = builder.year;
        this.month = builder.month;
        this.type = builder.type;
        this.fileMajorId = builder.fileId;
        this.fileMinorId = builder.fileSubId;
        this.fileType = builder.fileType;
    }

    /**
     * Builder Pattern Class
     */
    public static class Builder {
        @Setter
        private int year;
        @Setter
        private int month;
        @Setter
        private EnergyType type = null;
        @Setter
        private String fileId = StringUtils.EMPTY;
        @Setter
        private String fileSubId = StringUtils.EMPTY;
        @Setter
        private FileType fileType = FileType.UNKNOWN;

        public Builder setFileSubId(int sid) {
            this.fileSubId = String.format("%03d", sid);

            return this;
        }

        public Builder setYear(String $year) throws NumberFormatException {
            this.year = Integer.parseInt($year);

            return this;
        }

        public Builder setMonth(String $month) throws NumberFormatException {
            this.month = Integer.parseInt($month);

            return this;
        }

        /**
         * Builder Pattern Method
         *
         * @return
         */
        public EnergyFileEvent build() {
            return new EnergyFileEvent(this);
        }
    }
}
