package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminalBufferScrollbackTest {

    @Test
    void insertLineAtBottomOnBlankScreenStillRotatesLines() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);

        buffer.insertLineAtBottom();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals(Cell.EMPTY, buffer.getChar(0, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(0, 1));
    }

    @Test
    void maxScrollbackZeroDiscardsScrolledLines() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 0);
        buffer.writeText("abc");

        buffer.insertLineAtBottom();

        assertEquals(0, buffer.getScrollbackSize());
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getChar(0, -1));
    }

    @Test
    void negativeRowReadsFromScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.setForeground(Color.MAGENTA);
        buffer.writeText("AB");

        buffer.insertLineAtBottom();

        assertEquals('A', buffer.getChar(0, -1));
        assertEquals('B', buffer.getChar(1, -1));
        assertEquals(Color.MAGENTA, buffer.getAttributes(0, -1).foreground());
    }

    @Test
    void invalidRowsThrowIndexOutOfBounds() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 1);
        buffer.writeText("AB");
        buffer.insertLineAtBottom();

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getChar(0, -2));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getChar(0, 2));
    }

    @Test
    void insertLineAtBottomMovesTopLineAndGrowsScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.writeText("ABCD");
        buffer.setCursor(0, 1);
        buffer.writeText("WXYZ");

        buffer.insertLineAtBottom();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals('W', buffer.getChar(0, 0));
        assertEquals('X', buffer.getChar(1, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(0, 1));
    }

    @Test
    void insertLineAtBottomCapsScrollbackToMax() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);
        buffer.writeText("abc");
        buffer.insertLineAtBottom();

        buffer.writeText("def");
        buffer.insertLineAtBottom();

        assertEquals(1, buffer.getScrollbackSize());
    }

    @Test
    void clearScreenKeepsScrollbackIntact() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);
        buffer.writeText("abc");
        buffer.insertLineAtBottom();
        buffer.setCursor(0, 0);
        buffer.writeText("xy");

        buffer.clearScreen();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals(Cell.EMPTY, buffer.getChar(0, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(1, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(0, 1));
    }

    @Test
    void clearAllEmptiesScreenAndScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);
        buffer.writeText("abc");
        buffer.insertLineAtBottom();

        buffer.clearAll();

        assertEquals(0, buffer.getScrollbackSize());
        assertEquals(Cell.EMPTY, buffer.getChar(0, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(1, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(2, 0));
        assertEquals(Cell.EMPTY, buffer.getChar(0, 1));
    }
}

