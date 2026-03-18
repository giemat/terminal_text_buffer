package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferEditTest {

    @Test
    void fillLineFillsEntireRowWithCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        buffer.setForeground(Color.CYAN);

        buffer.fillLine(1, '#');

        for (int col = 0; col < 5; col++) {
            assertEquals('#', buffer.getChar(col, 1));
            assertEquals(Color.CYAN, buffer.getAttributes(col, 1).foreground());
        }
    }

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

    @Test
    void insertTextInMiddleShiftsContentRight() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 100);
        buffer.writeText("abcd");
        buffer.setCursor(2, 0);

        buffer.insertText("X");

        assertEquals('a', buffer.getChar(0, 0));
        assertEquals('b', buffer.getChar(1, 0));
        assertEquals('X', buffer.getChar(2, 0));
        assertEquals('c', buffer.getChar(3, 0));
        assertEquals('d', buffer.getChar(4, 0));
    }

    @Test
    void insertTextAtEndOfLineWrapsOverflowToNextRow() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.writeText("12345");
        buffer.setCursor(4, 0);

        buffer.insertText("Z");

        assertEquals('1', buffer.getChar(0, 0));
        assertEquals('2', buffer.getChar(1, 0));
        assertEquals('3', buffer.getChar(2, 0));
        assertEquals('4', buffer.getChar(3, 0));
        assertEquals('Z', buffer.getChar(4, 0));
        assertEquals('5', buffer.getChar(0, 1));
        assertEquals(1, buffer.getCursorCol());
        assertEquals(1, buffer.getCursorRow());
    }

    @Test
    void insertTextAtLastRowDropsOverflowGracefully() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 100);
        buffer.writeText("abc");
        buffer.setCursor(0, 1);
        buffer.writeText("XYZ");
        buffer.setCursor(2, 1);

        buffer.insertText("Q");

        assertEquals('X', buffer.getChar(0, 1));
        assertEquals('Y', buffer.getChar(1, 1));
        assertEquals('Q', buffer.getChar(2, 1));
        assertEquals(2, buffer.getCursorCol());
        assertEquals(1, buffer.getCursorRow());
    }
}

