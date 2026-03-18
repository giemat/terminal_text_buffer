package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferEditTest {

    @Test
    void writeTextWritesCharactersWithDefaultAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);

        buffer.writeText("Hello");

        assertEquals('H', buffer.getChar(0, 0));
        assertEquals('e', buffer.getChar(1, 0));
        assertEquals('l', buffer.getChar(2, 0));
        assertEquals('l', buffer.getChar(3, 0));
        assertEquals('o', buffer.getChar(4, 0));
        assertEquals(CellAttributes.DEFAULT, buffer.getAttributes(0, 0));
    }

    @Test
    void writeTextUsesCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.setForeground(Color.RED);

        buffer.writeText("Hi");

        assertEquals(Color.RED, buffer.getAttributes(0, 0).foreground());
        assertEquals(Color.RED, buffer.getAttributes(1, 0).foreground());
    }

    @Test
    void writeTextStopsAtLineWidthWithoutWrapping() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);

        buffer.writeText("abcdef");

        assertEquals('a', buffer.getChar(0, 0));
        assertEquals('b', buffer.getChar(1, 0));
        assertEquals('c', buffer.getChar(2, 0));
        assertEquals('d', buffer.getChar(3, 0));
        assertEquals('e', buffer.getChar(4, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(0, 1));
        assertEquals(4, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void writeTextOverwritesExistingContent() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.writeText("Hello");
        buffer.setCursor(0, 0);

        buffer.writeText("J");

        assertEquals('J', buffer.getChar(0, 0));
        assertEquals('e', buffer.getChar(1, 0));
        assertEquals('l', buffer.getChar(2, 0));
    }
}

