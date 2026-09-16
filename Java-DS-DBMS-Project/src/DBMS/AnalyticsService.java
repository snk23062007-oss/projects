package DBMS;

import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {
    private DBManager dbManager;
    public AnalyticsService(DBManager dbManager) { this.dbManager = dbManager; }

    public void getTopExceptions(String zone, int topN) {
        List<String[]> topExceptions = new ArrayList<>();
        try {
            String sql = "SELECT exception_type, SUM(count) AS total FROM error_daily_stats WHERE server_zone = ? AND exception_type <> 'None' GROUP BY exception_type ORDER BY total DESC LIMIT ?";
            PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
            stmt.setString(1, zone); stmt.setInt(2, topN);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) topExceptions.add(new String[]{rs.getString("exception_type"), rs.getString("total")});
        } catch (SQLException e) { System.out.println("Error fetching top exceptions: " + e.getMessage()); }
        printReport(topExceptions);
    }
    public void printReport(List<String[]> topExceptions) {
        System.out.println("\nTop Exceptions:");
        if (topExceptions.isEmpty()) System.out.println("No data found for this zone.");
        else { int rank = 1; for (String[] row : topExceptions) { System.out.println(rank + ". " + row[0] + " -> " + row[1] + " occurrences"); rank++; } }
    }
    public void reportBySeverity() {
        try {
            String sql = "SELECT severity, COUNT(*) AS total FROM log_events GROUP BY severity ORDER BY total DESC";
            Statement st = dbManager.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql);
            System.out.println("REPORT : logs by severity");
            while(rs.next()) System.out.println(rs.getString("severity")+" : "+rs.getInt("total"));
            st.close();
        } catch (Exception e) { System.out.println("Error while generation report : "+e.getMessage()); }
    }
    public void saveReportToFile() {
        try {
            Statement st = dbManager.getConnection().createStatement(); FileWriter fw = new FileWriter("report.txt");
            fw.write("------------------------------------------------------\n                LOG STATISTICAL REPORT                \n------------------------------------------------------\n\n");
            fw.write("--------------- COUNT BY EXCEPTION --------------------\n");
            ResultSet rs1 = st.executeQuery("SELECT exception_type, COUNT(*) AS total FROM log_events WHERE exception_type <> 'None' GROUP BY exception_type ORDER BY total DESC");
            while(rs1.next()) fw.write(rs1.getString("exception_type")+" -> "+rs1.getInt("total")+"\n");
            fw.write("\n--------------- COUNT BY SEVERITY --------------------\n");
            ResultSet rs2 = st.executeQuery("SELECT severity, COUNT(*) AS total FROM log_events GROUP BY severity ORDER BY total DESC");
            while(rs2.next()) fw.write(rs2.getString("severity")+" -> "+rs2.getInt("total")+"\n");
            fw.write("\n--------------- TOP EXCEPTION TYPES --------------------\n");
            ResultSet rs = st.executeQuery("SELECT exception_type, COUNT(*) AS total FROM log_events WHERE exception_type <> 'None' GROUP BY exception_type ORDER BY total DESC LIMIT 5");
            while(rs.next()) fw.write(rs.getString("exception_type")+" -> "+rs.getInt("total")+"\n");
            System.out.println("Reports saved successfully in 'report' text file"); fw.close(); st.close();
        } catch (Exception e) { System.out.println("Error saving report : "+e.getMessage()); }
    }
}
