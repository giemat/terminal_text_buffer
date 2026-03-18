package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferScrollbackTest {

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

