package com.termbuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TerminalBuffer {
    private int width;
    private int height;
    private final int maxScrollback;

    private final List<Line> screen;
    private final List<Line> scrollback;

    private int cursorCol;
    private int cursorRow;

    private CellAttributes currentAttributes;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }
        if (maxScrollback < 0) {
            throw new IllegalArgumentException("maxScrollback must be at least 0");
        }

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;

        this.screen = new ArrayList<>(height);
        for (int row = 0; row < height; row++) {
            this.screen.add(new Line(width));
        }
        this.scrollback = new ArrayList<>();

        this.cursorCol = 0;
        this.cursorRow = 0;
        this.currentAttributes = CellAttributes.DEFAULT;
    }

    public void setForeground(Color color) {
        currentAttributes = currentAttributes.withForeground(Objects.requireNonNull(color, "color must not be null"));
    }

    public void setBackground(Color color) {
        currentAttributes = currentAttributes.withBackground(Objects.requireNonNull(color, "color must not be null"));
    }

    public void addStyle(Style style) {
        currentAttributes = currentAttributes.withStyle(Objects.requireNonNull(style, "style must not be null"));
    }

    public void removeStyle(Style style) {
        currentAttributes = currentAttributes.withoutStyle(Objects.requireNonNull(style, "style must not be null"));
    }

    public void resetAttributes() {
        currentAttributes = CellAttributes.DEFAULT;
    }

    public CellAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    public int getCursorCol() {
        return cursorCol;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public void setCursor(int col, int row) {
        cursorCol = clamp(col, 0, width - 1);
        cursorRow = clamp(row, 0, height - 1);
    }

    public void moveCursor(int dcol, int drow) {
        setCursor(cursorCol + dcol, cursorRow + drow);
    }

    public void moveCursorUp(int n) {
        moveCursor(0, -n);
    }

    public void moveCursorDown(int n) {
        moveCursor(0, n);
    }

    public void moveCursorLeft(int n) {
        moveCursor(-n, 0);
    }

    public void moveCursorRight(int n) {
        moveCursor(n, 0);
    }

    public void writeText(String text) {
        Objects.requireNonNull(text, "text must not be null");
        Line line = screen.get(cursorRow);

        for (int i = 0; i < text.length(); i++) {
            if (cursorCol >= width) {
                break;
            }

            line.set(cursorCol, text.charAt(i), currentAttributes);
            cursorCol++;
        }

        cursorCol = clamp(cursorCol, 0, width - 1);
    }

    public void insertText(String text) {
        Objects.requireNonNull(text, "text must not be null");

        for (int i = 0; i < text.length(); i++) {
            Cell overflow = screen.get(cursorRow).insertAndReturnOverflow(cursorCol, text.charAt(i), currentAttributes);
            cursorCol++;

            while (cursorCol >= width) {
                if (cursorRow >= height - 1) {
                    cursorCol = width - 1;
                    return;
                }

                cursorRow++;
                cursorCol = 0;

                if (overflow.isEmpty()) {
                    break;
                }

                overflow = screen.get(cursorRow)
                        .insertAndReturnOverflow(cursorCol, overflow.getCh(), overflow.getAttributes());
                cursorCol++;
            }
        }

        setCursor(cursorCol, cursorRow);
    }

    public void fillLine(int row, char ch) {
        validateScreenRow(row);
        screen.get(row).fill(ch, currentAttributes);
    }

    public void insertLineAtBottom() {
        if (maxScrollback > 0) {
            scrollback.add(screen.get(0).copy());
            if (scrollback.size() > maxScrollback) {
                scrollback.remove(0);
            }
        }

        screen.remove(0);
        screen.add(new Line(width));
    }

    public void clearScreen() {
        for (int row = 0; row < height; row++) {
            screen.set(row, new Line(width));
        }
    }

    public void clearAll() {
        clearScreen();
        scrollback.clear();
    }

    public char getChar(int col, int row) {
        return resolveLine(row).get(col).getCh();
    }

    public CellAttributes getAttributes(int col, int row) {
        return resolveLine(row).get(col).getAttributes();
    }

    public String getLineAsString(int row) {
        return resolveLine(row).asString();
    }

    public String getScreenAsString() {
        return joinLines(screen);
    }

    public String getAllAsString() {
        if (scrollback.isEmpty()) {
            return getScreenAsString();
        }
        return joinLines(scrollback) + "\n" + joinLines(screen);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScrollbackSize() {
        return scrollback.size();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private Line resolveLine(int row) {
        if (row >= 0) {
            validateScreenRow(row);
            return screen.get(row);
        }

        int scrollbackIndex = scrollback.size() + row;
        if (scrollbackIndex < 0 || scrollbackIndex >= scrollback.size()) {
            throw new IndexOutOfBoundsException("Row out of bounds: " + row);
        }
        return scrollback.get(scrollbackIndex);
    }

    private String joinLines(List<Line> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(lines.get(i).asString());
        }
        return builder.toString();
    }

    private void validateScreenRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("Row out of bounds: " + row);
        }
    }
}
