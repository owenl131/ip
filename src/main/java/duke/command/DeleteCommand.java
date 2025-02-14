package duke.command;

import duke.DukeException;
import duke.Storage;
import duke.TaskList;
import duke.Ui;
import duke.parser.Parser;
import duke.task.Task;

/**
 * Command to remove a task from the list.
 */
public class DeleteCommand extends Command {
    public static final String KEYWORD = "delete";
    private final int index;

    /**
     * Creates a Delete command.
     * 
     * @param input The user input string
     */
    public DeleteCommand(String input) throws DukeException {
        super(input);
        String indexString = Parser.removeKeyword(input).trim();
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException e) {
            throw new DukeException(DukeException.getDidNotUnderstandIndexMessage(indexString));
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }

    @Override
    public TaskList execute(TaskList taskList, Ui ui, Storage storage, TaskList lastResults) throws DukeException {
        Task toDelete = lastResults.getItem(index);
        taskList.deleteItem(toDelete);
        ui.displayMessage(String.format("OK, I've deleted %s\n\n%s", toDelete, taskList.toString()));
        return taskList;
    }

}
