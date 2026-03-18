package com.termbuffer;

import java.util.Objects;

public class Line {
    private final Cell[] cells;

    public Line(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }

        this.cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            this.cells[i] = new Cell();
        }
    }

    public Cell get(int col) {
        validateColumn(col);
        return cells[col];
    }

    public void set(int col, char ch, CellAttributes attrs) {
        validateColumn(col);
        cells[col] = new Cell(ch, Objects.requireNonNull(attrs, "attrs must not be null"));
    }

    public void fill(char ch, CellAttributes attrs) {
        Objects.requireNonNull(attrs, "attrs must not be null");
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell(ch, attrs);
        }
    }

    public void insert(int col, char ch, CellAttributes attrs) {
        insertAndReturnOverflow(col, ch, attrs);
    }

    public Cell insertAndReturnOverflow(int col, char ch, CellAttributes attrs) {
        validateColumn(col);
        Objects.requireNonNull(attrs, "attrs must not be null");

        Cell overflow = cells[cells.length - 1].copy();

        for (int i = cells.length - 1; i > col; i--) {
            cells[i] = cells[i - 1];
        }

        cells[col] = new Cell(ch, attrs);
        return overflow;
    }

    public String asString() {
        int lastNonEmptyIndex = -1;
        for (int i = cells.length - 1; i >= 0; i--) {
            if (!cells[i].isEmpty()) {
                lastNonEmptyIndex = i;
                break;
            }
        }

        if (lastNonEmptyIndex < 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(lastNonEmptyIndex + 1);
        for (int i = 0; i <= lastNonEmptyIndex; i++) {
            char value = cells[i].getCh();
            if (value == Cell.WIDE_FILLER) {
                continue;
            }
            builder.append(value == Cell.EMPTY ? ' ' : value);
        }

        return builder.toString();
    }

    public Line copy() {
        Line copiedLine = new Line(cells.length);
        for (int i = 0; i < cells.length; i++) {
            copiedLine.cells[i] = cells[i].copy();
        }
        return copiedLine;
    }

    private void validateColumn(int col) {
        if (col < 0 || col >= cells.length) {
            throw new IndexOutOfBoundsException("Column out of bounds: " + col);
        }
    }
}

