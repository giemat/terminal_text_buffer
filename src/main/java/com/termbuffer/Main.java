package com.termbuffer;

import java.util.Locale;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Interactive Terminal Buffer");
            int width = readPositiveInt(scanner, "Width (> 0): ");
            int height = readPositiveInt(scanner, "Height (> 0): ");
            int maxScrollback = readNonNegativeInt(scanner, "Max scrollback (>= 0): ");

            TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

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
                case "fill" -> applyFill(buffer, remainder);
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

    private static void render(TerminalBuffer buffer) {
        String border = "+" + "-".repeat(buffer.getWidth()) + "+";
        System.out.println(border);

        for (int row = 0; row < buffer.getHeight(); row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < buffer.getWidth(); col++) {
                char ch = buffer.getChar(col, row);
                line.append(ch == Cell.EMPTY ? ' ' : ch);
            }
            System.out.println("|" + line + "|");
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
        System.out.println("  fill <row> <ch>   fill one row with a character");
        System.out.println("  scroll            push top line into scrollback");
        System.out.println("  clear             clear screen");
        System.out.println("  clearall          clear screen and scrollback");
        System.out.println("  show              redraw window");
        System.out.println("  help              show commands");
        System.out.println("  exit | quit       leave app");
        System.out.println("Tip: unrecognized input is treated as write text.");
    }
}

