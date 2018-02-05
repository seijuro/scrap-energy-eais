package com.github.seijuro.scrap.enegery.downloader.db.ctrl;

import com.github.seijuro.scrap.enegery.downloader.app.EnergyType;
import com.github.seijuro.scrap.enegery.downloader.db.record.DownloadHistoryRecord;
import com.github.seijuro.scrap.enegery.downloader.db.schema.DownloadHistoryTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Objects;

/**
 * Grouping all helper interfaces related to 'DownloadHistory'.
 */
class DownloadHistoryController {
    static final Logger LOG = LoggerFactory.getLogger(DownloadHistoryController.class);

    /**
     * DML(s)
     */
    static class DML {
        private static final String SelectLatesetEnergyRecord;
        private static final String SelectEnergyRecrodWhoseTypeAndYM;
        private static final String UpsertNewEnergyRecord;
        private static final String UpdateEnergyRecordStatus;

        static {
            //  SQL #1 : select latest download history record
            {
                StringBuffer query = new StringBuffer("SELECT ");
                query
                        .append(DownloadHistoryTable.Column.IDX.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILE_ID.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.TYPE.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.DATE_YM.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILEPATH.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.STATUS.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.LASTUPDATE.getName())
                        .append(" FROM ").append(DownloadHistoryTable.getName())
                        .append(" WHERE ").append(DownloadHistoryTable.Column.TYPE.getName()).append("=? ")
                        .append(" ORDER BY ").append(DownloadHistoryTable.Column.DATE_YM.getName()).append(" DESC")
                        .append(" LIMIT 1");
                SelectLatesetEnergyRecord = query.toString();
            }

            //  SQL #2 : Select download history record whose type & date is
            {
                StringBuffer query = new StringBuffer("SELECT ");
                query
                        .append(DownloadHistoryTable.Column.IDX.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILE_ID.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.TYPE.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.DATE_YM.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILEPATH.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.STATUS.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.LASTUPDATE.getName())
                        .append(" FROM ").append(DownloadHistoryTable.getName())
                        .append(" WHERE ").append(DownloadHistoryTable.Column.TYPE.getName()).append("=? ")
                        .append(" AND ").append(DownloadHistoryTable.Column.DATE_YM.getName()).append("=? ");

                SelectEnergyRecrodWhoseTypeAndYM = query.toString();
            }

            //  SQL #3 : Upsert energy record.
            {
                StringBuffer query = new StringBuffer("INSERT INTO ");

                query.append(DownloadHistoryTable.getName()).append("(")
                        .append(DownloadHistoryTable.Column.FILE_ID.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.TYPE.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.DATE_YM.getName()).append(", ")
                        .append(DownloadHistoryTable.Column.FILEPATH.getName()).append(") ")
                        .append(" VALUES(?, ?, ?, ?) ")
                        .append(" ON DUPLICATE KEY UPDATE ")
                        .append(DownloadHistoryTable.Column.TYPE.getName()).append("=VALUES(").append(DownloadHistoryTable.Column.TYPE.getName()).append("), ")
                        .append(DownloadHistoryTable.Column.DATE_YM.getName()).append("=VALUES(").append(DownloadHistoryTable.Column.DATE_YM.getName()).append("), ")
                        .append(DownloadHistoryTable.Column.FILEPATH.getName()).append("=VALUES(").append(DownloadHistoryTable.Column.FILEPATH.getName()).append(")");

                UpsertNewEnergyRecord = query.toString();
            }

            //  SQL #4 : Update energy status
            {
                StringBuffer query = new StringBuffer("UPDATE ");

                query.append(DownloadHistoryTable.getName())
                        .append(" SET ")
                        .append(DownloadHistoryTable.Column.STATUS.getName()).append("=? ")
                        .append(" WHERE ")
                        .append(DownloadHistoryTable.Column.FILE_ID.getName()).append("=? ");

                UpdateEnergyRecordStatus = query.toString();
            }
        }

        /**
         * retrieve lastest energy file that already downloaded.
         *
         * @param conn
         * @return
         * @throws SQLException
         */
        synchronized static DownloadHistoryRecord selectLatestEnergyFileFromDownloadHistory(Connection conn, EnergyType $type) throws SQLException {
            assert Objects.nonNull(conn);

            //  Log
            LOG.debug("prepared statement : {}", SelectLatesetEnergyRecord);
            PreparedStatement stmt = conn.prepareStatement(SelectLatesetEnergyRecord);

            //  Log
            LOG.debug("bind param : {1 : {}}", $type.toString());
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

        synchronized static DownloadHistoryRecord selectEnergyFileWhoseTypeAndDateYMIs(Connection conn, EnergyType $type, int $dateYM) throws SQLException {
            assert Objects.nonNull(conn);

            //  Log
            LOG.debug("prepared statement : {}", SelectEnergyRecrodWhoseTypeAndYM);
            PreparedStatement stmt = conn.prepareStatement(SelectEnergyRecrodWhoseTypeAndYM);

            //  Log
            LOG.debug("bind param : {1 : {}, 2 : {}}", $type.toString(), $dateYM);

            stmt.setString(1, $type.toString());
            stmt.setInt(2, $dateYM);

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

            //  Log
            LOG.debug("result : {}", result);

            return result;
        }

        /**
         * insert or update energy file.
         * it doesn't use 'BLOB' field(s).
         * record only contain the filepath.
         *
         * @param conn
         * @param $fileId
         * @param $type
         * @param $dateYM
         * @param $path
         * @return
         * @throws SQLException
         */
        synchronized static int upsertEnergyFile(Connection conn, String $fileId, EnergyType $type, int $dateYM, String $path) throws SQLException {
            assert Objects.nonNull(conn);

            //  Log
            LOG.debug("prepared statement : {}", UpsertNewEnergyRecord);

            PreparedStatement stmt = conn.prepareStatement(UpsertNewEnergyRecord);

            //  Log
            LOG.debug("bind param : {1 : {}, 2 : {}, 3 : {}, 4 : {}}", $fileId, $type.toString(), $dateYM, $path);

            stmt.setString(1, $fileId);
            stmt.setString(2, $type.toString());
            stmt.setInt(3, $dateYM);
            stmt.setString(4, $path);

            int affectedRows = stmt.executeUpdate();

            //  Log
            LOG.debug("closing statement ... (affected rows : {})", affectedRows);

            stmt.close();

            return affectedRows;
        }

        /**
         * update the status of energy file
         *
         * @param conn
         * @param $fileId
         * @param $status
         * @return
         * @throws SQLException
         */
        synchronized static int updateEnergyFileStatus(Connection conn, String $fileId, int $status) throws SQLException {
            assert Objects.nonNull(conn);

            //  Log
            LOG.debug("prepared statement : {}", UpdateEnergyRecordStatus);

            PreparedStatement stmt = conn.prepareStatement(UpdateEnergyRecordStatus);

            //  Log
            LOG.debug("bind param : {1 : {}, 2 : {}}", $fileId, $status);

            stmt.setInt(1, $status);
            stmt.setString(2, $fileId);

            int affectedRows = stmt.executeUpdate();

            //  Log
            LOG.debug("closing statement ... (affected rows : {})", affectedRows);

            stmt.close();

            return affectedRows;
        }

        /**
         * retrieve lastest 'electrocity' file that already downloaded.
         *
         * @param conn
         * @return
         * @throws SQLException
         */
        synchronized static DownloadHistoryRecord selectLatestElectrocityFileFromDownloadHistory(Connection conn) throws SQLException {
            return selectLatestEnergyFileFromDownloadHistory(conn, EnergyType.ELECTROCITY);
        }

        /**
         * retrieve lastest 'gas' file that already downloaded.
         *
         * @param conn
         * @return
         * @throws SQLException
         */
        synchronized static DownloadHistoryRecord selectLatestGasFileFromDownloadHistory(Connection conn) throws SQLException {
            return selectLatestEnergyFileFromDownloadHistory(conn, EnergyType.GAS);
        }
    }
}
