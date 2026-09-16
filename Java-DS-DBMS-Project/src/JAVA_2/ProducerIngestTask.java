package JAVA_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

class ProducerIngestTask extends Thread {
    BlockingQueue<String> rawQueue;
    String filePath;
    String zone;
    String POISON_PILL = "END";

    public ProducerIngestTask(BlockingQueue<String> rawQueue, String filePath, String zone) {
        this.rawQueue = rawQueue;
        this.filePath = filePath;
        this.zone = (zone != null) ? zone : inferZoneFromSource(filePath);
    }
    public void run() {
        try { readAllLines(); }
        catch (Exception e) { System.out.println(e); }
        finally { signalEnd(); }
    }
    void readAllLines() throws IOException {
        try(BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = fileReader.readLine()) != null) rawQueue.put(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error Producing Log : "+e.getMessage());
        }
    }
    void signalEnd() {
        try { rawQueue.put(POISON_PILL); }
        catch(InterruptedException e) { Thread.currentThread().interrupt(); }
    }
    String inferZoneFromSource(String filePath) {
        String name = new File(filePath).getName();
        return name.contains("-") ? name.substring(0, name.indexOf("-")) : "unknown";
    }
}
