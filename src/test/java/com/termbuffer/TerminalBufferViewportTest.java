package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferViewportTest {

    @Test
    void viewOffsetMapsVisibleRowsIntoScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.writeText("L0");
        buffer.setCursor(0, 1);
        buffer.writeText("L1");
        buffer.insertLineAtBottom();

        buffer.setCursor(0, 1);
        buffer.writeText("L2");
        buffer.insertLineAtBottom();

        assertEquals("L2", buffer.getVisibleLineAsString(0));
        assertEquals("", buffer.getVisibleLineAsString(1));

        buffer.setViewOffset(1);
        assertEquals(1, buffer.getViewOffset());
        assertEquals("L1", buffer.getVisibleLineAsString(0));
        assertEquals("L2", buffer.getVisibleLineAsString(1));

        buffer.setViewOffset(2);
        assertEquals(2, buffer.getViewOffset());
        assertEquals("L0", buffer.getVisibleLineAsString(0));
        assertEquals("L1", buffer.getVisibleLineAsString(1));
    }

    @Test
    void viewOffsetIsClampedAndDoesNotChangeNegativeRowApi() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.writeText("A");
        buffer.setCursor(0, 1);
        buffer.writeText("B");
        buffer.insertLineAtBottom();

        buffer.setCursor(0, 0);
        buffer.writeText("C");
        buffer.insertLineAtBottom();

        buffer.setViewOffset(999);

        assertEquals(2, buffer.getViewOffset());
        assertEquals("C", buffer.getLineAsString(-1));
        assertEquals("", buffer.getLineAsString(0));

        buffer.clearAll();
        assertEquals(0, buffer.getViewOffset());
    }
}



