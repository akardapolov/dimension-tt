package ru.dimension.tt.schema;

import ru.dimension.tt.annotation.ColumnKind;

public record TTColumnSpec(
    String id,
    int order,
    String name,
    ColumnKind kind,
    boolean visible,
    boolean editable,
    int minWidth,
    int maxWidth,
    int preferredWidth
) {}