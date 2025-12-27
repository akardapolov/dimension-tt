package ru.dimension.tt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import ru.dimension.tt.schema.TTColumnBinding;
import ru.dimension.tt.schema.TTSchema;

public final class TTTableModel<T> extends AbstractTableModel {

  private final TTSchema<T> schema;
  private final List<T> items = new ArrayList<>();

  public TTTableModel(TTSchema<T> schema) {
    this.schema = schema;
  }

  public TTSchema<T> schema() {
    return schema;
  }

  public List<T> items() {
    return Collections.unmodifiableList(items);
  }

  public void setItems(List<T> newItems) {
    items.clear();
    items.addAll(newItems);
    fireTableDataChanged();
  }

  public void addItem(T item) {
    int idx = items.size();
    items.add(item);
    fireTableRowsInserted(idx, idx);
  }

  public void clear() {
    if (items.isEmpty()) return;
    items.clear();
    fireTableDataChanged();
  }

  public T itemAt(int modelRow) {
    return items.get(modelRow);
  }

  @Override
  public int getRowCount() {
    return items.size();
  }

  @Override
  public int getColumnCount() {
    return schema.columnCount();
  }

  @Override
  public String getColumnName(int column) {
    return schema.column(column).spec().name();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return schema.column(columnIndex).columnClass();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    TTColumnBinding<T> c = schema.column(columnIndex);
    return c.spec().editable() && c.setter() != null;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    T row = items.get(rowIndex);
    TTColumnBinding<T> c = schema.column(columnIndex);
    Object v = c.getter().apply(row);
    return v;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    TTColumnBinding<T> c = schema.column(columnIndex);
    if (c.setter() == null) return;

    T row = items.get(rowIndex);
    c.setter().accept(row, aValue);
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}