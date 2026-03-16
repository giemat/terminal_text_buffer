package com.termbuffer;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record CellAttributes(Color foreground, Color background, Set<Style> styles) {
    public static final CellAttributes DEFAULT =
            new CellAttributes(Color.DEFAULT, Color.DEFAULT, EnumSet.noneOf(Style.class));

    public CellAttributes {
        foreground = Objects.requireNonNull(foreground, "foreground must not be null");
        background = Objects.requireNonNull(background, "background must not be null");
        styles = copyStyles(styles);
    }

    public CellAttributes withForeground(Color color) {
        return new CellAttributes(Objects.requireNonNull(color, "color must not be null"), background, styles);
    }

    public CellAttributes withBackground(Color color) {
        return new CellAttributes(foreground, Objects.requireNonNull(color, "color must not be null"), styles);
    }

    public CellAttributes withStyle(Style style) {
        EnumSet<Style> nextStyles = toMutableEnumSet(styles);
        nextStyles.add(Objects.requireNonNull(style, "style must not be null"));
        return new CellAttributes(foreground, background, nextStyles);
    }

    public CellAttributes withoutStyle(Style style) {
        EnumSet<Style> nextStyles = toMutableEnumSet(styles);
        nextStyles.remove(Objects.requireNonNull(style, "style must not be null"));
        return new CellAttributes(foreground, background, nextStyles);
    }

    public CellAttributes withStyles(Set<Style> styles) {
        return new CellAttributes(foreground, background, styles);
    }

    private static Set<Style> copyStyles(Set<Style> styles) {
        if (styles == null || styles.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(EnumSet.copyOf(styles));
    }

    private static EnumSet<Style> toMutableEnumSet(Set<Style> styles) {
        if (styles.isEmpty()) {
            return EnumSet.noneOf(Style.class);
        }
        return EnumSet.copyOf(styles);
    }
}

