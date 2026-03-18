package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferCursorTest {

    @Test
    void cursorStartsAtOrigin() {
        TerminalBuffer buffer = new TerminalBuffer(10, 4, 100);

        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void setCursorClampsToBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 4, 100);

        buffer.setCursor(50, 20);
        assertEquals(9, buffer.getCursorCol());
        assertEquals(3, buffer.getCursorRow());

        buffer.setCursor(-5, -10);
        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorRightStopsAtLastColumn() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);

        buffer.moveCursorRight(999);

        assertEquals(4, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorDownStopsAtLastRow() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);

        buffer.moveCursorDown(999);

        assertEquals(0, buffer.getCursorCol());
        assertEquals(2, buffer.getCursorRow());
    }

    @Test
    void negativeMovesStopAtZero() {
        TerminalBuffer buffer = new TerminalBuffer(8, 6, 100);
        buffer.setCursor(3, 4);

        buffer.moveCursorLeft(100);
        buffer.moveCursorUp(100);

        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }
}

