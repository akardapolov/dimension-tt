package ru.dimension.tt.schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record TTColumnBinding<T>(
    TTColumnSpec spec,
    Class<?> columnClass,
    Function<T, Object> getter,
    BiConsumer<T, Object> setter // nullable
) {}