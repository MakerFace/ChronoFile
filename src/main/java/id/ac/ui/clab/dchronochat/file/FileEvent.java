package id.ac.ui.clab.dchronochat.file;

public class FileEvent {
    public EventType type;
    public Object content;

    public enum EventType {
        INFO,
        SENDER,
        RECEIVER
    }
}
