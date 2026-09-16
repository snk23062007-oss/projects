package JAVA_2;

import java.time.LocalDateTime;
import java.util.Objects;

public class LogRecords {
    int eventId;
    String sourceName;
    String zone;
    LocalDateTime eventTime;
    public String severity;
    String exceptionType;
    String message;
    String stackTrace;

    public LogRecords(int eventId, String sourceName, String zone, LocalDateTime eventTime, String severity, String exceptionType, String message, String stackTrace) {
        this.eventId = eventId;
        this.sourceName = sourceName;
        this.zone = zone;
        this.eventTime = eventTime;
        this.severity = severity;
        this.exceptionType = exceptionType;
        this.message = message;
        this.stackTrace = stackTrace;
    }
    public int getEventId() { return eventId; }
    public String getSourceName() { return sourceName; }
    public String getZone() { return zone; }
    public LocalDateTime getEventTime() { return eventTime; }
    public String getSeverity() { return severity; }
    public String getExceptionType() { return exceptionType; }
    public String getMessage() { return message; }
    public String getStackTrace() { return stackTrace; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setZone(String zone) { this.zone = zone; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public void setMessage(String message) { this.message = message; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    @Override public String toString() {
        return String.format("%-6d | %-20s | %-8s | %-20s | %-8s | %-30s | %-70s | %-30s",
            eventId, Objects.toString(sourceName, ""), Objects.toString(zone, ""), Objects.toString(eventTime, ""),
            Objects.toString(severity, ""), Objects.toString(exceptionType, ""), Objects.toString(message, ""), Objects.toString(stackTrace, ""));
    }
}
