package JAVA_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import DS.InMemoryIndex;
import DBMS.DBManager;

class SearchService {
    private InMemoryIndex index;
    private DBManager dbManager;
    public SearchService(InMemoryIndex index, DBManager dbManager) { this.index = index; this.dbManager = dbManager; }

    public void searchByException(String exceptionType) {
        List<LogRecords> records = new ArrayList<>();
        records = index.searchByExceptionType(exceptionType);
        String queryText = null;
        System.out.println("\nSearch results for : " + exceptionType);
        if (records.isEmpty()) { System.out.println("   No records found."); return; }
        else {
            String fmt = "%-6s | %-20s | %-8s | %-20s | %-8s | %-30s | %-70s | %-30s";
            System.out.println(String.join("", Collections.nCopies(210, "-")));
            System.out.println(String.format(fmt, "ID", "Source", "Zone", "Event Time", "Severity", "Exception", "Message", "StackTrace"));
            System.out.println(String.join("", Collections.nCopies(210, "-")));
            for (LogRecords record : records) { queryText = record.exceptionType; System.out.println(record); }
        }
        System.out.println(String.join("", Collections.nCopies(210, "-")));
        System.out.println("Total found: " + records.size());
        dbManager.logSearch(queryText, "ERROR", records.size());
    }

    public void searchBySeverity(String severity) {
        List<LogRecords> records = new ArrayList<>();
        records = index.searchBySeverity(severity);
        String queryText = null;
        System.out.println("\nSearch results for : " + severity);
        if (records.isEmpty()) { System.out.println("   No records found."); return; }
        else {
            String fmt = "%-6s | %-20s | %-8s | %-20s | %-8s | %-30s | %-70s | %-30s";
            System.out.println(String.join("", Collections.nCopies(210, "-")));
            System.out.println(String.format(fmt, "ID", "Source", "Zone", "Event Time", "Severity", "Exception", "Message", "StackTrace"));
            System.out.println(String.join("", Collections.nCopies(210, "-")));
            for (LogRecords record : records) { queryText = record.exceptionType; System.out.println(record); }
        }
        System.out.println(String.join("", Collections.nCopies(210, "-")));
        System.out.println("Total found: " + records.size());
        dbManager.logSearch(queryText, severity, records.size());
    }
}
