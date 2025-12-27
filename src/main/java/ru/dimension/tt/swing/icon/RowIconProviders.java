package ru.dimension.tt.swing.icon;

import java.util.Objects;
import java.util.function.Function;
import javax.swing.Icon;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTColumnBinding;
import ru.dimension.tt.schema.TTSchema;

public final class RowIconProviders {
  private RowIconProviders() {}

  public static <T, V> RowIconProvider<T> byColumn(
      TTSchema<T> schema,
      String columnId,
      Class<V> valueType,
      Function<V, Icon> iconFn
  ) {
    return byColumn(schema, columnId, valueType, iconFn, v -> null);
  }

  public static <T, V> RowIconProvider<T> byColumn(
      TTSchema<T> schema,
      String columnId,
      Class<V> valueType,
      Function<V, Icon> iconFn,
      Function<V, String> tooltipFn
  ) {
    Objects.requireNonNull(schema, "schema");
    Objects.requireNonNull(columnId, "columnId");
    Objects.requireNonNull(valueType, "valueType");
    Objects.requireNonNull(iconFn, "iconFn");
    Objects.requireNonNull(tooltipFn, "tooltipFn");

    int modelIdx = schema.modelIndexOf(columnId);
    TTColumnBinding<T> col = schema.column(modelIdx);

    return row -> {
      Object raw = col.getter().apply(row);
      if (raw == null) {
        return new RowIconProvider.RowIcon(null, null);
      }
      if (!valueType.isInstance(raw)) {
        return new RowIconProvider.RowIcon(null, null);
      }
      V v = valueType.cast(raw);
      return new RowIconProvider.RowIcon(iconFn.apply(v), tooltipFn.apply(v));
    };
  }

  public static <T, E extends Enum<E>> RowIconProvider<T> byEnumColumn(
      TTSchema<T> schema,
      String columnId,
      Class<E> enumType,
      IconMapper<E> mapper
  ) {
    Objects.requireNonNull(mapper, "mapper");
    return byColumn(schema, columnId, enumType, mapper::iconFor, mapper::tooltipFor);
  }

  public static <T, E extends Enum<E>> RowIconProvider<T> byEnumColumn(
      TTTableModel<T> model,
      String columnId,
      Class<E> enumType,
      IconMapper<E> mapper
  ) {
    return byEnumColumn(model.schema(), columnId, enumType, mapper);
  }
}