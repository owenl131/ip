package duke;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import duke.command.AddCommand;
import duke.command.Command;
import duke.command.DeleteCommand;
import duke.command.ExitCommand;
import duke.command.ListCommand;
import duke.command.MarkCommand;
import duke.command.UnmarkCommand;
import duke.command.FindCommand;

/**
 * The parser handles parsing of commands.
 */
public class Parser {

    /**
     * Parses a user input string as a command.
     * 
     * @param description User input string
     * @return The command corresponding to the user input
     * @throws DukeException Throws an exception if the input is malformed
     */
    public static Command parseCommand(String description) throws DukeException {
        String keyword = parseKeyword(description);
        switch (keyword) {
        case "bye":
            return new ExitCommand(description);
        case "list":
            return new ListCommand(description);
        case "mark":
            return new MarkCommand(description);
        case "unmark":
            return new UnmarkCommand(description);
        case "delete":
            return new DeleteCommand(description);
        case "deadline":
        case "event":
        case "todo":
            return new AddCommand(description);
        case "find":
            return new FindCommand(description);
        default:
            throw new DukeException("I don't understand this command!");
        }
    }

    /**
     * Extracts the command keyword from the user input.
     * 
     * @param description The user input string
     * @return The keyword
     */
    public static String parseKeyword(String description) {
        return description.split(" ")[0];
    }

    /**
     * Drops the command keyword from the user input string.
     * 
     * @param description The user input string
     * @return The user input string with the keyword removed
     */
    public static String removeKeyword(String description) {
        int firstSpace = description.indexOf(' ');
        if (firstSpace == -1) {
            return "";
        }
        String withoutKeyword = description.substring(firstSpace + 1);
        return withoutKeyword;
    }

    /**
     * Extracts the contents of the user input string that is not part of the
     * parameter list.
     * 
     * @param description The user input string
     * @return The command contents
     */
    public static String parseName(String description) {
        String withoutKeyword = removeKeyword(description);
        int firstSlash = withoutKeyword.indexOf('/');
        if (firstSlash != -1) {
            String withoutParams = withoutKeyword.substring(0, firstSlash - 1);
            return withoutParams.trim();
        }
        return withoutKeyword.trim();
    }

    /**
     * Extracts the parameters in the user input.
     * 
     * @param description The user input string
     * @return A map from the parameter key to parameter value
     */
    public static Map<String, String> parseParams(String description) {
        Map<String, String> paramsMap = new TreeMap<>();
        int firstSlash = description.indexOf('/');
        if (firstSlash == -1) {
            return paramsMap;
        }
        String paramsString = description.substring(firstSlash + 1);
        for (String param : paramsString.split(" /")) {
            int firstSpace = param.indexOf(' ');
            String key = param.substring(0, firstSpace).trim();
            String value = param.substring(firstSpace + 1).trim();
            paramsMap.put(key, value);
        }
        return paramsMap;
    }

    /**
     * Parses a date string to a LocalDate object. Can handle date formats such as
     * 2022-12-25, 25-12-2022, 2022/12/25, 25/12/2022, 25 dec, 25 December 2022. If
     * the year is not provided, such as 25 dec, then it defaults to the current
     * year.
     * 
     * @param input The date string to be parsed
     * @return A LocalDate object, or null if the date cannot be parsed
     */
    public static LocalDate parseDateString(String input) {
        if (input.contains(" ")) {
            for (String part : input.split(" ")) {
                LocalDate attempt = parseDateString(part); // attempt to process recursively
                if (attempt != null) {
                    return attempt;
                }
            }
        } else if (input.contains("-") || input.contains("/")) {
            String[] components = input.split("[-/]");
            if (components.length != 3) {
                return null;
            }
            boolean yearFirst = components[0].length() == 4;
            int year = Integer.parseInt(yearFirst ? components[0] : components[2]);
            int month = Integer.parseInt(components[1]);
            int day = Integer.parseInt(yearFirst ? components[2] : components[0]);
            return LocalDate.of(year, month, day);
        }
        try {
            Pattern yearPattern = Pattern.compile("\\b(2\\d{3})\\b"); // four digit number starting with 2
            Matcher yearMatcher = yearPattern.matcher(input);
            // default to current year
            int year = yearMatcher.find() ? Integer.parseInt(yearMatcher.group()) : LocalDate.now().getYear();
            List<String> months = List.of("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov",
                    "dec");
            Pattern monthPattern = Pattern.compile("(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)");
            Matcher monthMatcher = monthPattern.matcher(input.toLowerCase());
            if (!monthMatcher.find()) {
                return null;
            }
            int month = months.indexOf(monthMatcher.group()) + 1;
            Pattern dayPattern = Pattern.compile("\\b(\\d{1,2})\\b");
            Matcher dayMatcher = dayPattern.matcher(input);
            if (!dayMatcher.find()) {
                return null;
            }
            int day = Integer.parseInt(dayMatcher.group());
            return LocalDate.of(year, month, day);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date time string to a LocalTime object. The time string must contain
     * a ':' between the hours and minutes and can optionally specify am/pm/AM/PM.
     * 
     * @param input The time string to be parsed
     * @return A LocalTime object or null if the time string cannot be parsed
     */
    public static LocalTime parseTimeString(String input) {
        input = input.trim();
        // if input contains spaces, then it may contain both a
        // date component and time component
        // attempt to parse each component and take the one that succeeds
        if (input.contains(" ")) {
            for (String part : input.split(" ")) {
                LocalTime attempt = parseTimeString(part);
                if (attempt != null) {
                    return attempt;
                }
            }
            return null;
        }
        // assume that the time must be formatted as HH:MM(AM|PM)?
        if (!input.contains(":")) {
            return null;
        }
        String suffix = "";
        input = input.toUpperCase();
        if (input.endsWith("PM")) {
            suffix = "PM";
            input = input.replace("PM", "");
        } else if (input.endsWith("AM")) {
            suffix = "AM";
            input = input.replace("AM", "");
        }
        try {
            String[] components = input.split(":");
            int hours = Integer.parseInt(components[0]);
            int minutes = Integer.parseInt(components[1]);
            if (suffix.equals("PM")) {
                hours += 12;
            } else if (suffix.equals("AM") && hours == 12) {
                hours -= 12;
            }
            return LocalTime.of(hours, minutes);
        } catch (NumberFormatException e) {
            return null;
        } catch (DateTimeException e) {
            return null;
        }
    }
}
