package com.github.seijuro.scrap.enegery.downloader.db.record;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class DownloadHistoryRecord {
    public enum Status {
        INIT(0),
        PROGRESS(1),
        DONE(2);

        /**
         * Instance Properties
         */
        @Getter
        private final int code;

        /**
         * C'tor
         *
         * @param $code
         */
        Status(int $code) {
            this.code = $code;
        }

        /**
         * convert the given status code(integer) into <code>Status</code> instance.
         *
         * @param code
         * @return
         */
        public static Status parseInt(int code) {
            for (Status status : Status.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }

            return null;
        }
    }

    /**
     * Instance Properties
     */
    private final long idx;
    private final String fileId;
    private final EnergyType type;
    private final int dateYM;
    private final String filepath;
    private final Status status;
    private final java.util.Date lastupdate;


    /**
     * (Builder Pattern) C'tor
     *
     * @param builder
     */
    protected DownloadHistoryRecord(Builder builder) {
        this.idx = builder.idx;
        this.fileId = builder.fileId;
        this.type = builder.type;
        this.dateYM = builder.dateYM;
        this.filepath = builder.filepath;
        this.status = builder.status;
        this.lastupdate = builder.lastupdate;
    }

    /**
     * Builder Pattner Class
     */
    public static class Builder {
        /**
         * Instance Properties
         */
        @Setter
        private long idx = Long.MIN_VALUE;
        @Setter
        private String fileId = StringUtils.EMPTY;
        @Setter
        private EnergyType type = null;
        @Setter
        private Status status = null;
        @Setter
        private int dateYM = Integer.MIN_VALUE;
        @Setter
        private String filepath = StringUtils.EMPTY;
        @Setter
        private java.util.Date lastupdate = null;

        /**
         * (Ovderloading) property(type) setter.
         *
         * @param $type
         * @return
         * @throws IllegalArgumentException
         */
        @SuppressWarnings("unused")
        public Builder setEnergyType(String $type) throws IllegalArgumentException {
            for (EnergyType type : EnergyType.values()) {
                if (type.toString().equalsIgnoreCase($type)) {
                    this.type = type;

                    return this;
                }
            }

            throw new IllegalArgumentException("Parameter, $type, is not valid.");
        }

        /**
         * (Ovderloading) property(status) setter.
         *
         * @param $code
         * @return
         * @throws IllegalArgumentException
         */
        @SuppressWarnings("unused")
        public Builder setStatus(int $code) throws IllegalArgumentException {
            this.status = Status.parseInt($code);

            if (Objects.nonNull(this.status)) {
                throw new IllegalArgumentException(String.format("Parameter, code, is not valid (code : %d)", $code));
            }

            return this;
        }

        /**
         * Builder Pattern method
         *
         * @return
         */
        public DownloadHistoryRecord build() {
            return new DownloadHistoryRecord(this);
        }
    }
}
