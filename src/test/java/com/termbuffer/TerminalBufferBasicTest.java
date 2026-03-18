package com.termbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminalBufferBasicTest {

    @Test
    void terminalBufferConstructorAcceptsValidDimensions() {
        assertDoesNotThrow(() -> new TerminalBuffer(80, 24, 1000));
    }

    @Test
    void terminalBufferExposesConfiguredDimensions() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

        assertEquals(80, buffer.getWidth());
        assertEquals(24, buffer.getHeight());
    }

    @Test
    void terminalBufferConstructorRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 24, 1000));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 24, -1));
    }

    @Test
    void emptyLineAsStringReturnsEmptyString() {
        Line line = new Line(5);

        assertEquals("", line.asString());
    }

    @Test
    void fillReplacesEveryCell() {
        Line line = new Line(4);
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(Color.GREEN);

        line.fill('x', attrs);

        for (int i = 0; i < 4; i++) {
            assertEquals('x', line.get(i).getCh());
            assertEquals(attrs, line.get(i).getAttributes());
        }
    }

    @Test
    void insertShiftsRightAndDropsLastCell() {
        Line line = new Line(5);
        CellAttributes attrs = CellAttributes.DEFAULT;
        line.set(0, 'a', attrs);
        line.set(1, 'b', attrs);
        line.set(2, 'c', attrs);
        line.set(3, 'd', attrs);
        line.set(4, 'e', attrs);

        line.insert(2, 'X', attrs);

        assertEquals('a', line.get(0).getCh());
        assertEquals('b', line.get(1).getCh());
        assertEquals('X', line.get(2).getCh());
        assertEquals('c', line.get(3).getCh());
        assertEquals('d', line.get(4).getCh());
    }

    @Test
    void copyIsIndependentFromOriginal() {
        Line original = new Line(3);
        CellAttributes attrs = CellAttributes.DEFAULT.withStyle(Style.BOLD);
        original.set(0, 'A', attrs);
        original.set(1, 'B', attrs);

        Line copied = original.copy();
        original.set(1, 'Z', CellAttributes.DEFAULT);

        assertEquals('B', copied.get(1).getCh());
        assertEquals(attrs, copied.get(1).getAttributes());
        assertNotSame(original.get(1), copied.get(1));
    }
}

