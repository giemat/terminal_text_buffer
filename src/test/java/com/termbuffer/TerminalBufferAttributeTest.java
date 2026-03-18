package com.termbuffer;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalBufferAttributeTest {

    @Test
    void freshScreenCellsExposeDefaultAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        assertEquals(CellAttributes.DEFAULT, buffer.getAttributes(0, 0));
        assertEquals(CellAttributes.DEFAULT, buffer.getAttributes(3, 1));
    }

    @Test
    void terminalBufferStartsWithDefaultCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        assertEquals(CellAttributes.DEFAULT, buffer.getCurrentAttributes());
    }

    @Test
    void terminalBufferAttributeMutatorsUpdateCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        buffer.setForeground(Color.RED);
        buffer.setBackground(Color.BLUE);
        buffer.addStyle(Style.BOLD);
        buffer.addStyle(Style.UNDERLINE);
        buffer.removeStyle(Style.BOLD);

        CellAttributes current = buffer.getCurrentAttributes();
        assertEquals(Color.RED, current.foreground());
        assertEquals(Color.BLUE, current.background());
        assertEquals(Set.of(Style.UNDERLINE), current.styles());
    }

    @Test
    void terminalBufferResetAttributesRestoresDefault() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setForeground(Color.GREEN);
        buffer.addStyle(Style.ITALIC);

        buffer.resetAttributes();

        assertEquals(CellAttributes.DEFAULT, buffer.getCurrentAttributes());
    }

    @Test
    void defaultAttributesUseDefaultColorsAndNoStyles() {
        CellAttributes attributes = CellAttributes.DEFAULT;

        assertEquals(Color.DEFAULT, attributes.foreground());
        assertEquals(Color.DEFAULT, attributes.background());
        assertTrue(attributes.styles().isEmpty());
    }

    @Test
    void withStyleAndWithoutStyleDoNotMutateOriginal() {
        CellAttributes base = CellAttributes.DEFAULT;
        CellAttributes withBold = base.withStyle(Style.BOLD);
        CellAttributes removedBold = withBold.withoutStyle(Style.BOLD);

        assertTrue(base.styles().isEmpty());
        assertTrue(withBold.styles().contains(Style.BOLD));
        assertFalse(removedBold.styles().contains(Style.BOLD));
        assertTrue(removedBold.styles().isEmpty());
    }

    @Test
    void multipleStylesCanCoexist() {
        CellAttributes attributes = CellAttributes.DEFAULT
                .withStyle(Style.BOLD)
                .withStyle(Style.ITALIC)
                .withStyle(Style.UNDERLINE);

        assertEquals(Set.of(Style.BOLD, Style.ITALIC, Style.UNDERLINE), attributes.styles());
    }

    @Test
    void constructorDefensivelyCopiesStyles() {
        Set<Style> source = Set.of(Style.BOLD);
        CellAttributes attributes = new CellAttributes(Color.RED, Color.BLACK, source);

        assertEquals(Set.of(Style.BOLD), attributes.styles());
        assertEquals(Color.RED, attributes.foreground());
        assertEquals(Color.BLACK, attributes.background());
    }
}

