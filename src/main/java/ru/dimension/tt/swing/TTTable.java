package ru.dimension.tt.swing;

import java.util.Optional;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import ru.dimension.tt.model.TTTableModel;

public final class TTTable<T, TABLE extends JTable> {

  private final TABLE table;
  private final TTTableModel<T> model;
  private final JScrollPane scrollPane;

  public TTTable(TABLE table, TTTableModel<T> model, JScrollPane scrollPane) {
    this.table = table;
    this.model = model;
    this.scrollPane = scrollPane;
  }

  public TABLE table() {
    return table;
  }

  public TTTableModel<T> model() {
    return model;
  }

  public JScrollPane scrollPane() {
    return scrollPane;
  }

  public void setItems(java.util.List<T> items) {
    model.setItems(items);
  }

  public void addItem(T item) {
    model.addItem(item);
  }

  public Optional<T> selectedItem() {
    int viewRow = table.getSelectedRow();
    if (viewRow < 0) return Optional.empty();
    int modelRow = table.convertRowIndexToModel(viewRow);
    if (modelRow < 0 || modelRow >= model.getRowCount()) return Optional.empty();
    return Optional.of(model.itemAt(modelRow));
  }
}