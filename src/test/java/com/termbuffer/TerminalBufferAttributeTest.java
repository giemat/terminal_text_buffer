package com.termbuffer;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalBufferAttributeTest {

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

