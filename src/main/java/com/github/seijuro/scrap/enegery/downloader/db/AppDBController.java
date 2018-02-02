package com.github.seijuro.scrap.enegery.downloader.db;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import com.github.seijuro.scrap.enegery.downloader.db.record.DownloadHistoryRecord;
import com.github.seijuro.scrap.enegery.downloader.db.schema.DownloadHistoryTable;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

public class AppDBController extends MySQLDBController {
    /**
     * create method to instantiate controller.
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     * @return
     */
    public static AppDBController create(String $host, String $user, String $passwd, String $database) {
        return new AppDBController($host, null, $user, $passwd, $database);
    }

    /**
     * C'tor
     *
     * @param $host
     * @param $user
     * @param $passwd
     * @param $database
     */
    protected AppDBController(String $host, String $port, String $user, String $passwd, String $database) {
        super($host, $port, $user, $passwd, $database);
    }

    static class DownloadHistory {
        static class DML {
            @Getter
            private static final String SelectLatesetRecord;

            static {
                StringBuffer query = new StringBuffer("SELECT ");
                query
                        .append(DownloadHistoryTable.Column.IDX.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILE_ID.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILE_TYPE.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.DATE_YM.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILEPATH.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.STATUS.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.LAStUPDATE.getName())
                        .append(" FROM ").append(DownloadHistoryTable.getName())
                        .append(" WHERE " ).append(DownloadHistoryTable.Column.FILE_TYPE.getName()).append("=? ")
                        .append(" ORDER BY ").append(DownloadHistoryTable.Column.DATE_YM.getName()).append(" DESC")
                        .append(" LIMIT 1");
                SelectLatesetRecord = query.toString();
            }

            /**
             * retrieve lastest energy file that already downloaded.
             *
             * @param conn
             * @return
             * @throws SQLException
             */
            static DownloadHistoryRecord selectLatestEnergyFileFromDownloadHistory(Connection conn, EnergyType $type) throws SQLException {
                assert Objects.nonNull(conn);

                PreparedStatement stmt = conn.prepareStatement(SelectLatesetRecord);
                stmt.setString(1, $type.toString());

                ResultSet rs = stmt.executeQuery();
                DownloadHistoryRecord result = null;

                if (rs.next()) {
                    DownloadHistoryRecord.Builder recordBuilder = new DownloadHistoryRecord.Builder();

                    recordBuilder.setIdx(rs.getLong(1));
                    recordBuilder.setFileId(rs.getString(2));
                    recordBuilder.setType(rs.getString(3));
                    recordBuilder.setDateYM(rs.getInt(4));
                    recordBuilder.setFilepath(rs.getString(5));
                    recordBuilder.setStatus(rs.getInt(6));
                    recordBuilder.setLastupdate(rs.getTimestamp(7));

                    result = recordBuilder.build();
                }

                rs.close();
                stmt.close();

                return result;
            }

            /**
             * retrieve lastest 'electrocity' file that already downloaded.
             *
             * @param conn
             * @return
             * @throws SQLException
             */
            static DownloadHistoryRecord selectLatestElectrocityFileFromDownloadHistory(Connection conn) throws SQLException {
                return selectLatestEnergyFileFromDownloadHistory(conn, EnergyType.ELECTROCITY);
            }

            /**
             * retrieve lastest 'gas' file that already downloaded.
             *
             * @param conn
             * @return
             * @throws SQLException
             */
            static DownloadHistoryRecord selectLatestGasFileFromDownloadHistory(Connection conn) throws SQLException {
                return selectLatestEnergyFileFromDownloadHistory(conn, EnergyType.GAS);
            }
        }
    }
}
