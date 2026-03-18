package com.termbuffer;

import java.util.Objects;

public class Cell {
    public static final char EMPTY = '\0';
    public static final char WIDE_FILLER = '\uFFFE';

    private char ch;
    private CellAttributes attributes;

    public Cell() {
        this(EMPTY, CellAttributes.DEFAULT);
    }

    public Cell(char ch, CellAttributes attributes) {
        this.ch = ch;
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    public char getCh() {
        return ch;
    }

    public void setCh(char ch) {
        this.ch = ch;
    }

    public CellAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(CellAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    public boolean isEmpty() {
        return ch == EMPTY;
    }

    public boolean isWideFiller() {
        return ch == WIDE_FILLER;
    }

    public Cell copy() {
        return new Cell(ch, attributes);
    }
}

