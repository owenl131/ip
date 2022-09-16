package duke.task;

import duke.DukeException;

public class EventTask extends Task {
    private String eventDateTime;

    public EventTask(String name, String eventDateTime) throws DukeException {
        this(name, eventDateTime, false);
    }

    public EventTask(String name, String eventDateTime, boolean status) throws DukeException {
        super(name, status);
        if ("".equals(name)) {
            throw new DukeException("Event name cannot be empty");
        }
        if (eventDateTime == null) {
            throw new DukeException("Please provide a date and time (/at)");
        }
        this.eventDateTime = eventDateTime;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (at: " + eventDateTime + ")";
    }

    @Override
    public String serialize() {
        return String.format("event %s /at %s /done %s", getName(), eventDateTime, isDone());
    }

}
