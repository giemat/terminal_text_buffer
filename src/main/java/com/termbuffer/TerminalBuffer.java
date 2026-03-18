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

    public char getChar(int col, int row) {
        validateRow(row);
        return screen.get(row).get(col).getCh();
    }

    public CellAttributes getAttributes(int col, int row) {
        validateRow(row);
        return screen.get(row).get(col).getAttributes();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("Row out of bounds: " + row);
        }
    }
}
