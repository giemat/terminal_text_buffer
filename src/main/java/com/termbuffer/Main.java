package com.termbuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class Main {
    private static final String ANSI_ESCAPE_PREFIX = "\u001b[";
    private static final String ANSI_RESET = "\u001b[0m";

    private Main() {
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Interactive Terminal Buffer");
            int width = readPositiveInt(scanner, "Width (> 0): ");
            int height = readPositiveInt(scanner, "Height (> 0): ");
            int maxScrollback = readNonNegativeInt(scanner, "Max scrollback (>= 0): ");
            Color foreground = readColor(scanner, "Foreground color");
            Color background = readColor(scanner, "Background color");
            List<Style> styles = readStyles(scanner, "Styles (comma-separated or 'none')");

            TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);
            buffer.setForeground(foreground);
            buffer.setBackground(background);
            for (Style style : styles) {
                buffer.addStyle(style);
            }

            printHelp();
            render(buffer);

            while (true) {
                System.out.print("tb> ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\nExiting.");
                    return;
                }

                String input = scanner.nextLine();
                if (input.isBlank()) {
                    continue;
                }

                if (!applyCommand(buffer, input)) {
                    break;
                }

                render(buffer);
            }
        }
    }

    private static boolean applyCommand(TerminalBuffer buffer, String input) {
        String trimmed = input.trim();
        String[] parts = trimmed.split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String remainder = parts.length > 1 ? parts[1] : "";

        try {
            switch (command) {
                case "help" -> printHelp();
                case "show" -> {
                }
                case "write" -> buffer.writeText(remainder);
                case "insert" -> buffer.insertText(remainder);
                case "cursor" -> applyCursor(buffer, remainder);
                case "h" -> buffer.moveCursorLeft(parseMovementSteps(remainder));
                case "j" -> buffer.moveCursorDown(parseMovementSteps(remainder));
                case "k" -> buffer.moveCursorUp(parseMovementSteps(remainder));
                case "l" -> buffer.moveCursorRight(parseMovementSteps(remainder));
                case "fill" -> applyFill(buffer, remainder);
                case "style" -> applyStyle(buffer, remainder);
                case "fg" -> buffer.setForeground(parseColorArgument(remainder, "Usage: fg <color>"));
                case "bg" -> buffer.setBackground(parseColorArgument(remainder, "Usage: bg <color>"));
                case "attr", "attrs", "attribute" -> applyAttributes(buffer, remainder);
                case "getch", "char" -> applyGetChar(buffer, remainder);
                case "getattr", "ga" -> applyGetAttributes(buffer, remainder);
                case "getline", "line" -> applyGetLine(buffer, remainder);
                case "getscreen", "screen" -> printBlock("Screen", buffer.getScreenAsString());
                case "getall", "all" -> printBlock("Scrollback + Screen", buffer.getAllAsString());
                case "scroll" -> buffer.insertLineAtBottom();
                case "clear" -> buffer.clearScreen();
                case "clearall" -> buffer.clearAll();
                case "quit", "exit" -> {
                    System.out.println("Goodbye.");
                    return false;
                }
                default -> {
                    // Treat unknown input as plain text write for quick interaction.
                    buffer.writeText(trimmed);
                }
            }
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        return true;
    }

    private static void applyCursor(TerminalBuffer buffer, String remainder) {
        String[] values = remainder.trim().split("\\s+");
        if (values.length != 2) {
            throw new IllegalArgumentException("Usage: cursor <col> <row>");
        }

        int col = Integer.parseInt(values[0]);
        int row = Integer.parseInt(values[1]);
        buffer.setCursor(col, row);
    }

    private static void applyFill(TerminalBuffer buffer, String remainder) {
        String[] values = remainder.trim().split("\\s+", 2);
        if (values.length != 2 || values[1].isEmpty()) {
            throw new IllegalArgumentException("Usage: fill <row> <char>");
        }

        int row = Integer.parseInt(values[0]);
        char ch = values[1].charAt(0);
        buffer.fillLine(row, ch);
    }

    private static void applyStyle(TerminalBuffer buffer, String remainder) {
        String trimmed = remainder.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Usage: style <add|rm|clear|show> [styles]");
        }

        String[] parts = trimmed.split("\\s+", 2);
        String action = parts[0].toLowerCase(Locale.ROOT);
        String args = parts.length > 1 ? parts[1].trim() : "";

        switch (action) {
            case "add", "+" -> {
                for (Style style : parseStylesInput(args, false)) {
                    buffer.addStyle(style);
                }
            }
            case "rm", "remove", "-" -> {
                for (Style style : parseStylesInput(args, false)) {
                    buffer.removeStyle(style);
                }
            }
            case "clear", "none", "reset" -> {
                for (Style style : Style.values()) {
                    if (style != Style.DEFAULT) {
                        buffer.removeStyle(style);
                    }
                }
            }
            case "show" -> System.out.println("Current styles: " + buffer.getCurrentAttributes().styles());
            default -> throw new IllegalArgumentException("Usage: style <add|rm|clear|show> [styles]");
        }
    }

    private static void applyAttributes(TerminalBuffer buffer, String remainder) {
        String action = remainder.trim().toLowerCase(Locale.ROOT);
        if (action.isEmpty() || action.equals("show")) {
            CellAttributes current = buffer.getCurrentAttributes();
            System.out.println("Current attributes: fg=" + current.foreground()
                    + " bg=" + current.background()
                    + " styles=" + current.styles());
            return;
        }

        if (action.equals("reset") || action.equals("clear")) {
            buffer.resetAttributes();
            return;
        }

        throw new IllegalArgumentException("Usage: attr <show|reset>");
    }

    private static void applyGetChar(TerminalBuffer buffer, String remainder) {
        int[] pos = parseColRow(remainder, "Usage: getch <col> <row>");
        char ch = buffer.getChar(pos[0], pos[1]);
        System.out.println("char[" + pos[0] + "," + pos[1] + "]=" + formatChar(ch));
    }

    private static void applyGetAttributes(TerminalBuffer buffer, String remainder) {
        int[] pos = parseColRow(remainder, "Usage: getattr <col> <row>");
        CellAttributes attributes = buffer.getAttributes(pos[0], pos[1]);
        System.out.println("attrs[" + pos[0] + "," + pos[1] + "]=" + attributes);
    }

    private static void applyGetLine(TerminalBuffer buffer, String remainder) {
        int row = parseRow(remainder, "Usage: getline <row>");
        String line = buffer.getLineAsString(row);
        System.out.println("line[" + row + "]=" + quote(line));
    }

    private static int[] parseColRow(String remainder, String usage) {
        String[] values = remainder.trim().split("\\s+");
        if (values.length != 2) {
            throw new IllegalArgumentException(usage);
        }

        return new int[]{Integer.parseInt(values[0]), Integer.parseInt(values[1])};
    }

    private static int parseRow(String remainder, String usage) {
        String token = remainder.trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException(usage);
        }
        return Integer.parseInt(token);
    }

    private static String formatChar(char ch) {
        if (ch == Cell.EMPTY) {
            return "<EMPTY>";
        }
        if (ch == Cell.WIDE_FILLER) {
            return "<WIDE_FILLER>";
        }
        return quote(Character.toString(ch));
    }

    private static String quote(String value) {
        return "\"" + value + "\"";
    }

    private static void printBlock(String title, String content) {
        System.out.println(title + ":");
        if (content.isEmpty()) {
            System.out.println("<EMPTY>");
            return;
        }
        System.out.println(content);
    }

    private static Color parseColorArgument(String remainder, String usage) {
        String token = remainder.trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException(usage);
        }

        try {
            return Color.valueOf(token.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown color: " + token + ". Available: " + joinEnumNames(Color.values()));
        }
    }

    private static int parseMovementSteps(String remainder) {
        String trimmed = remainder.trim();
        if (trimmed.isEmpty()) {
            return 1;
        }

        int steps = Integer.parseInt(trimmed);
        if (steps <= 0) {
            throw new IllegalArgumentException("Movement steps must be greater than 0");
        }
        return steps;
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(raw);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Keep prompting until input is valid.
            }

            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    private static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(raw);
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Keep prompting until input is valid.
            }

            System.out.println("Please enter a whole number 0 or greater.");
        }
    }

    private static Color readColor(Scanner scanner, String label) {
        String options = joinEnumNames(Color.values());
        while (true) {
            System.out.print(label + " [" + options + "]: ");
            String raw = scanner.nextLine().trim();

            if (raw.isEmpty()) {
                return Color.DEFAULT;
            }

            try {
                return Color.valueOf(raw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                System.out.println("Unknown color. Try one of: " + options);
            }
        }
    }

    private static List<Style> readStyles(Scanner scanner, String label) {
        String options = joinStyleNames();
        while (true) {
            System.out.print(label + " [" + options + "]: ");
            String raw = scanner.nextLine().trim();

            try {
                return parseStylesInput(raw, true);
            } catch (IllegalArgumentException ex) {
                System.out.println("Unknown style. Try a comma-separated list from: " + options);
            }
        }
    }

    private static List<Style> parseStylesInput(String raw, boolean allowNoneKeyword) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            if (allowNoneKeyword) {
                return List.of();
            }
            throw new IllegalArgumentException("At least one style is required");
        }

        if (allowNoneKeyword && (trimmed.equalsIgnoreCase("none") || trimmed.equalsIgnoreCase("default"))) {
            return List.of();
        }

        String[] tokens = trimmed.split(",");
        List<Style> styles = new ArrayList<>();
        for (String token : tokens) {
            String value = token.trim();
            if (value.isEmpty()) {
                continue;
            }

            Style style = Style.valueOf(value.toUpperCase(Locale.ROOT));
            if (style != Style.DEFAULT && !styles.contains(style)) {
                styles.add(style);
            }
        }

        if (styles.isEmpty() && !allowNoneKeyword) {
            throw new IllegalArgumentException("At least one style is required");
        }
        return styles;
    }

    private static String joinEnumNames(Enum<?>[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values[i].name());
        }
        return builder.toString();
    }

    private static String joinStyleNames() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Style style : Style.values()) {
            if (style == Style.DEFAULT) {
                continue;
            }
            if (!first) {
                builder.append(", ");
            }
            builder.append(style.name());
            first = false;
        }
        return builder.toString();
    }

    private static void render(TerminalBuffer buffer) {
        boolean ansiEnabled = isAnsiEnabled();
        String border = "+" + "-".repeat(buffer.getWidth()) + "+";
        System.out.println(border);

        for (int row = 0; row < buffer.getHeight(); row++) {
            StringBuilder line = new StringBuilder();
            CellAttributes previousAttributes = null;
            line.append('|');

            for (int col = 0; col < buffer.getWidth(); col++) {
                if (ansiEnabled) {
                    CellAttributes currentAttributes = buffer.getAttributes(col, row);
                    if (!currentAttributes.equals(previousAttributes)) {
                        line.append(toAnsi(currentAttributes));
                        previousAttributes = currentAttributes;
                    }
                }

                char ch = buffer.getChar(col, row);
                line.append(ch == Cell.EMPTY ? ' ' : ch);
            }

            if (ansiEnabled && previousAttributes != null) {
                line.append(ANSI_RESET);
            }
            line.append('|');
            System.out.println(line);
        }

        System.out.println(border);
        System.out.println("cursor=(" + buffer.getCursorCol() + "," + buffer.getCursorRow() + ")"
                + " scrollback=" + buffer.getScrollbackSize());
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  write <text>      write text at cursor");
        System.out.println("  insert <text>     insert text at cursor");
        System.out.println("  cursor <c> <r>    set cursor position");
        System.out.println("  h [n]             move cursor right by n (default 1)");
        System.out.println("  j [n]             move cursor down by n (default 1)");
        System.out.println("  k [n]             move cursor up by n (default 1)");
        System.out.println("  l [n]             move cursor left by n (default 1)");
        System.out.println("  style add <list>  add styles (comma-separated)");
        System.out.println("  style rm <list>   remove styles (comma-separated)");
        System.out.println("  style clear       clear all styles");
        System.out.println("  style show        show active styles");
        System.out.println("  fg <color>        set foreground color");
        System.out.println("  bg <color>        set background color");
        System.out.println("  attr show         show active attributes");
        System.out.println("  attr reset        reset attributes to defaults");
        System.out.println("  getch <c> <r>     get character at col,row");
        System.out.println("  getattr <c> <r>   get cell attributes at col,row");
        System.out.println("  getline <r>       get line text at row");
        System.out.println("  getscreen         get full visible screen text");
        System.out.println("  getall            get full scrollback + screen text");
        System.out.println("  fill <row> <ch>   fill one row with a character");
        System.out.println("  scroll            push top line into scrollback");
        System.out.println("  clear             clear screen");
        System.out.println("  clearall          clear screen and scrollback");
        System.out.println("  show              redraw window");
        System.out.println("  help              show commands");
        System.out.println("  exit | quit       leave app");
        System.out.println("Rows: 0..height-1 = screen, negative rows = scrollback (-1 newest).");
        System.out.println("Tip: unrecognized input is treated as write text.");
    }

    private static boolean isAnsiEnabled() {
        String noColor = System.getenv("NO_COLOR");
        if (noColor != null) {
            return false;
        }

        String term = System.getenv("TERM");
        return term != null && !term.equalsIgnoreCase("dumb");
    }

    private static String toAnsi(CellAttributes attributes) {
        StringBuilder sequence = new StringBuilder(ANSI_ESCAPE_PREFIX);
        sequence.append('0');
        sequence.append(';').append(foregroundCode(attributes.foreground()));
        sequence.append(';').append(backgroundCode(attributes.background()));

        for (Style style : attributes.styles()) {
            int styleCode = styleCode(style);
            if (styleCode > 0) {
                sequence.append(';').append(styleCode);
            }
        }

        sequence.append('m');
        return sequence.toString();
    }

    private static int styleCode(Style style) {
        return switch (style) {
            case BOLD -> 1;
            case ITALIC -> 3;
            case UNDERLINE -> 4;
            case STRIKETHROUGH -> 9;
            case DEFAULT -> -1;
        };
    }

    private static int foregroundCode(Color color) {
        return switch (color) {
            case DEFAULT -> 39;
            case BLACK -> 30;
            case RED -> 31;
            case GREEN -> 32;
            case YELLOW -> 33;
            case BLUE -> 34;
            case MAGENTA -> 35;
            case CYAN -> 36;
            case WHITE -> 37;
            case BRIGHT_BLACK -> 90;
            case BRIGHT_RED -> 91;
            case BRIGHT_GREEN -> 92;
            case BRIGHT_YELLOW -> 93;
            case BRIGHT_BLUE -> 94;
            case BRIGHT_MAGENTA -> 95;
            case BRIGHT_CYAN -> 96;
            case BRIGHT_WHITE -> 97;
        };
    }

    private static int backgroundCode(Color color) {
        return switch (color) {
            case DEFAULT -> 49;
            case BLACK -> 40;
            case RED -> 41;
            case GREEN -> 42;
            case YELLOW -> 43;
            case BLUE -> 44;
            case MAGENTA -> 45;
            case CYAN -> 46;
            case WHITE -> 47;
            case BRIGHT_BLACK -> 100;
            case BRIGHT_RED -> 101;
            case BRIGHT_GREEN -> 102;
            case BRIGHT_YELLOW -> 103;
            case BRIGHT_BLUE -> 104;
            case BRIGHT_MAGENTA -> 105;
            case BRIGHT_CYAN -> 106;
            case BRIGHT_WHITE -> 107;
        };
    }
}

