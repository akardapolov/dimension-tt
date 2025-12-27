package ru.dimension.tt.schema;

import java.util.*;

public final class TTSchema<T> {

  private final Class<T> rowType;
  private final List<TTColumnBinding<T>> columns;
  private final Map<String, Integer> modelIndexById;

  public TTSchema(Class<T> rowType, List<TTColumnBinding<T>> columns) {
    this.rowType = rowType;
    this.columns = List.copyOf(columns);

    Map<String, Integer> map = new HashMap<>();
    for (int i = 0; i < this.columns.size(); i++) {
      map.put(this.columns.get(i).spec().id(), i);
    }
    this.modelIndexById = Collections.unmodifiableMap(map);
  }

  public Class<T> rowType() {
    return rowType;
  }

  public List<TTColumnBinding<T>> columns() {
    return columns;
  }

  public int columnCount() {
    return columns.size();
  }

  public String[] columnNames() {
    return columns.stream().map(c -> c.spec().name()).toArray(String[]::new);
  }

  public TTColumnBinding<T> column(int modelIndex) {
    return columns.get(modelIndex);
  }

  public int modelIndexOf(String columnId) {
    Integer idx = modelIndexById.get(columnId);
    if (idx == null) throw new IllegalArgumentException("Unknown columnId: " + columnId);
    return idx;
  }
}