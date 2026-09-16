package DBMS;

import java.sql.*;
import JAVA_2.LogRecords;
import JAVA_2.AppMain;

public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/log_monitor";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection conn;

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB connected successfully.");
        } catch (Exception e) { System.out.println("Failed to connect to DB: " + e.getMessage()); }
    }

    public int insertLogSourceIfNeeded(String sourceName, String zone, String filePath) {
        int sourceId = -1;
        try {
            String checkSql = "SELECT source_id FROM log_sources WHERE source_name = ? AND zone = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, sourceName); checkStmt.setString(2, zone);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) sourceId = rs.getInt("source_id");
            else {
                String insertSql = "INSERT INTO log_sources (source_name, zone, file_path) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, sourceName); insertStmt.setString(2, zone); insertStmt.setString(3, filePath);
                insertStmt.executeUpdate();
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) sourceId = keys.getInt(1);
            }
        } catch (SQLException e) { System.out.println("Error in insertLogSourceIfNeeded: " + e.getMessage()); }
        return sourceId;
    }

    public int insertLogEvent(LogRecords r) {
        int eventId = -1;
        try {
            int sourceId = insertLogSourceIfNeeded(r.getSourceName(), r.getZone(), AppMain.filePath);
            String sql = "INSERT IGNORE INTO log_events (source_id, event_time, severity, exception_type, message, stack_trace, server_zone) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, sourceId); stmt.setTimestamp(2, Timestamp.valueOf(r.getEventTime()));
            stmt.setString(3, r.getSeverity()); stmt.setString(4, r.getExceptionType()); stmt.setString(5, r.getMessage());
            stmt.setString(6, r.getStackTrace()); stmt.setString(7, r.getZone()); stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) { eventId = keys.getInt(1); r.setEventId(eventId); }
        } catch (SQLException e) { System.out.println("Error inserting log event: " + e.getMessage()); }
        return eventId;
    }

    public void logSearch(String query_text, String severity, int size) {
        try {
            String sql = "INSERT INTO search_audit(query_text, severity_filter, result_count) VALUES(?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, query_text); pst.setString(2, severity); pst.setInt(3, size); pst.executeUpdate();
        } catch (Exception e) { System.out.println("Error while inserting search in search history : "+e.getMessage()); }
    }
    public void showExceptionTypes() {
        try {
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT exception_name FROM error_types GROUP BY exception_name");
            System.out.println("\n-------- EXCEPTION TYPES --------");
            while(rs.next()) System.out.println(rs.getString("exception_name"));
            System.out.println("-----------------------------------");
        } catch (Exception e) { System.out.println("Error showing exception types : "+e.getMessage()); }
    }
    public void showSearchHistory() {
        try {
            String sql = "SELECT query_text, severity_filter, executed_at, result_count FROM search_audit";
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            System.out.println("\n-------- SEARCH HISTORY --------");
            while(rs.next()) System.out.println("Searched -> "+rs.getString("query_text")+", Severity : "+rs.getString(2)+"\nRecords Found : "+rs.getInt(4)+" at "+rs.getTimestamp(3)+"\n");
            System.out.println("--------------------------------"); st.close();
        } catch (Exception e) { System.out.println("Error fetching history : "+e.getMessage()); }
    }
    public Connection getConnection() { return conn; }
    public void close() {
        try { if (conn != null) { conn.close(); System.out.println("DB connection closed."); } }
        catch (SQLException e) { System.out.println("Error closing DB connection: " + e.getMessage()); }
    }
}
