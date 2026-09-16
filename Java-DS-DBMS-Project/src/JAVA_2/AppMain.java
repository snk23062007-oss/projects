package JAVA_2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import DBMS.DBManager;
import DS.InMemoryIndex;
import DBMS.AnalyticsService;


public class AppMain {
    public static String filePath = "C:\\javaprogram\\Java-DS-DBMS Project SEM-2\\Log Files\\";
    static String zone;

    static Map<String, List<Integer>> keywordIndex = new HashMap<>();
    static Map<String, TreeMap<LocalDateTime, List<Integer>>> timeIndex = new HashMap<>();
    static Map<Integer, LogRecords> recordStore = new HashMap<>();

    static InMemoryIndex index = new InMemoryIndex(keywordIndex, timeIndex, recordStore);
    static DBManager dbManager = new DBManager();

    static SearchService searchService = new SearchService(index, dbManager);
    static AnalyticsService analyticsService = new AnalyticsService(dbManager);
    static Scanner sc = new Scanner(System.in);
    static boolean isLoaded = false;

    public static void main(String[] args) {
        dbManager.connect();
        mainMenu();
    }

    static void mainMenu() {
        while(true) {
            System.out.println("\n------------- MINI LOG MONITOR --------------");
            System.out.println("|  1. Load logs from file                   |");
            System.out.println("|  2. Show all logs in memory               |");
            System.out.println("|  3. Search                                |");
            System.out.println("|  4. Show statistical report               |");
            System.out.println("|  5. Show search history                   |");
            System.out.println("|  6. Save report to file                   |");
            System.out.println("|  0. Exit                                  |");
            System.out.println("---------------------------------------------");
            System.out.print("Enter your choice : ");

            String choice = sc.nextLine().trim();

            switch(choice) {
                case "1" -> {
                    if(isLoaded) {
                        System.out.println("Logs already loaded.");
                    } else {
                        loadLogs();
                    }
                }

                case "2" -> showAllLogs();
                case "3" -> searchMenu();
                case "4" -> reportMenu();
                case "5" -> dbManager.showSearchHistory();
                case "6" -> analyticsService.saveReportToFile();
                case "0" -> {
                    dbManager.close();
                    sc.close();
                    System.out.println("Exiting....");
                    System.exit(0);
                }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    static void loadLogs() {
        File path = new File(filePath);

        if (!path.exists()) {
            System.out.println("Invalid path configured in filePath: " + filePath);
            return;
        }

        if (path.isFile()) {
            loadFiles(List.of(path));
            isLoaded = true;
            return;
        }

        List<File> availableFiles = getLogFiles(path);
        if (availableFiles.isEmpty()) {
            System.out.println("No log files found at: " + filePath);
            return;
        }

        while (true) {
            System.out.println("\nChoose load mode:");
            System.out.println("1. Load all files from directory");
            System.out.println("2. Select one or more files to load");
            System.out.println("0. Exit to Main Menu");
            System.out.print("Enter your choice: ");

            String option = sc.nextLine().trim();
            switch (option) {
                case "1" -> { loadFiles(availableFiles); return; }
                case "2" -> {
                    List<File> selectedFiles = chooseFilesToLoad(availableFiles);
                    if (selectedFiles.isEmpty()) {
                        System.out.println("No files selected. Load canceled.");
                        return;
                    }
                    loadFiles(selectedFiles);
                    return;
                }
                case "0" -> { System.out.println("Load canceled."); return; }
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 0.");
            }
        }
    }

    static void loadFiles(List<File> filesToLoad) {
        if (filesToLoad.isEmpty()) {
            System.out.println("No files to load.");
            return;
        }

        BlockingQueue<String> rawQueue = new LinkedBlockingQueue<>();
        List<ProducerIngestTask> producers = new ArrayList<>();

        for (File file : filesToLoad) {
            ProducerIngestTask producer = new ProducerIngestTask(rawQueue, file.getAbsolutePath(), zone);
            producers.add(producer);
            producer.start();
        }

        ParserWorker parser = new ParserWorker(rawQueue, index, dbManager, producers.size());
        parser.start();

        try {
            for (ProducerIngestTask producer : producers) producer.join();
            parser.join();
            System.out.println(index.recordStore.size() + " Logs Loaded Successfully.");
            isLoaded = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Log loading interrupted.");
        }
    }

    static List<File> getLogFiles(File directory) {
        File[] logFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));
        if (logFiles == null) return new ArrayList<>();
        return Arrays.asList(logFiles);
    }

    static List<File> chooseFilesToLoad(List<File> files) {
        System.out.println("\nAvailable log files:");
        for (int i = 0; i < files.size(); i++) System.out.println((i + 1) + ". " + files.get(i).getName());
        System.out.println("Enter file numbers separated by commas or ranges (e.g. 1,3,5-7):");
        System.out.print("Selection: ");
        return parseSelection(sc.nextLine().trim(), files);
    }

    static List<File> parseSelection(String selection, List<File> files) {
        Set<Integer> selectedIndexes = new HashSet<>();
        String[] tokens = selection.split(",");
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;
            if (token.contains("-")) {
                String[] range = token.split("-");
                if (range.length != 2) { System.out.println("Skipping invalid range: " + token); continue; }
                try {
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    if (start > end) { System.out.println("Skipping invalid range: " + token); continue; }
                    for (int i = start; i <= end; i++) if (i >= 1 && i <= files.size()) selectedIndexes.add(i - 1);
                } catch (NumberFormatException e) { System.out.println("Skipping invalid range: " + token); }
            } else {
                try {
                    int index = Integer.parseInt(token);
                    if (index >= 1 && index <= files.size()) selectedIndexes.add(index - 1);
                    else System.out.println("Skipping invalid selection: " + token);
                } catch (NumberFormatException e) { System.out.println("Skipping invalid selection: " + token); }
            }
        }
        List<File> selectedFiles = new ArrayList<>();
        for (Integer index : selectedIndexes) selectedFiles.add(files.get(index));
        return selectedFiles;
    }

    static void showAllLogs() {
        List<LogRecords> logs = new ArrayList<>(index.recordStore.values());
        if(logs.isEmpty()) { System.out.println("\nNo logs available in memory"); return; }
        String fmt = "%-6s | %-20s | %-8s | %-20s | %-8s | %-30s | %-70s | %-30s";
        System.out.println("\n-------- All logs in memory --------");
        System.out.println(String.join("", Collections.nCopies(210, "-")));
        System.out.println(String.format(fmt, "ID", "Source", "Zone", "Event Time", "Severity", "Exception", "Message", "StackTrace"));
        System.out.println(String.join("", Collections.nCopies(210, "-")));
        for(LogRecords log : logs) System.out.println(log);
        System.out.println(String.join("", Collections.nCopies(210, "-")));
        System.out.println("Total Logs : "+logs.size()+"\n");
    }

    static void searchMenu() {
        while(true) {
            System.out.println("\n-------- SEARCH BY --------");
            System.out.println("|  1. Exception Type      |");
            System.out.println("|  2. Severity            |");
            System.out.println("|  0. Exit to main menu   |");
            System.out.println("---------------------------");
            System.out.print("Enter your choice : ");
            String searchChoice = sc.nextLine().trim();
            switch (searchChoice) {
                case "1" -> {
                    dbManager.showExceptionTypes();
                    System.out.print("Enter Exception Type (e.g. SQLException) : ");
                    searchService.searchByException(sc.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Enter Severity (INFO/WARNING/ERROR/FATAL) : ");
                    searchService.searchBySeverity(sc.nextLine().trim());
                }
                case "0" -> { return; }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    static void reportMenu() {
        while(true) {
            System.out.println("\n-------- STATISTICAL REPORT OF --------");
            System.out.println("|  1. Total count by severity         |");
            System.out.println("|  2. Total count by keyword          |");
            System.out.println("|  3. Top exception types             |");
            System.out.println("|  0. Exit to main menu               |");
            System.out.println("---------------------------------------");
            System.out.print("Enter your choice : ");
            String reportChoice = sc.nextLine().trim();
            switch (reportChoice) {
                case "1" -> analyticsService.reportBySeverity();
                case "2" -> {
                    System.out.print("Enter keyword to find in messages: ");
                    String keyword = sc.nextLine().trim();
                    List<LogRecords> found = index.searchByKeyword(keyword);
                    System.out.println("\nLogs of Messages containing '" + keyword + "':");
                    String fmt1 = "%-6s | %-20s | %-8s | %-20s | %-8s | %-30s | %-70s | %-30s";
                    System.out.println(String.join("", Collections.nCopies(210, "-")));
                    System.out.println(String.format(fmt1, "ID", "Source", "Zone", "Event Time", "Severity", "Exception", "Message", "StackTrace"));
                    System.out.println(String.join("", Collections.nCopies(210, "-")));
                    for (LogRecords r : found) System.out.println(r);
                    System.out.println(String.join("", Collections.nCopies(210, "-")));
                    System.out.println("Total count: " + found.size());
                }
                case "3" -> {
                    String selectedZone;
                    while(true) {
                        System.out.print("Enter Zone (Central/North/South/East/West) : ");
                        selectedZone = sc.nextLine();
                        if(selectedZone.equalsIgnoreCase("central") || selectedZone.equalsIgnoreCase("north") || selectedZone.equalsIgnoreCase("south") || selectedZone.equalsIgnoreCase("east") || selectedZone.equalsIgnoreCase("west")) break;
                        System.out.println("No data found for this zone.");
                    }
                    analyticsService.getTopExceptions(selectedZone, 5);
                }
                case "0" -> { return; }
                default -> System.out.println("Invalid Choice");
            }
        }
    }
}
