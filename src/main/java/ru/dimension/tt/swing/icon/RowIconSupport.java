package ru.dimension.tt.swing.icon;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import ru.dimension.tt.model.TTTableModel;

public final class RowIconSupport {
  private RowIconSupport() {}

  public static <T> void install(
      JTable table,
      TTTableModel<T> model,
      RowIconPlacement placement,
      RowIconProvider<T> provider
  ) {
    if (placement instanceof RowIconPlacement.InColumn inCol) {
      installInColumn(table, model, inCol.columnId(), provider);
      return;
    }
    throw new IllegalArgumentException("Unsupported placement: " + placement);
  }

  public static <T> void installInColumn(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      RowIconProvider<T> provider
  ) {
    int modelCol = model.schema().modelIndexOf(columnId);
    int viewCol = table.convertColumnIndexToView(modelCol);
    if (viewCol < 0) return;

    // base renderer: column renderer OR default renderer for column class
    var column = table.getColumnModel().getColumn(viewCol);
    TableCellRenderer base = column.getCellRenderer();
    if (base == null) {
      base = table.getDefaultRenderer(model.getColumnClass(modelCol));
    }
    if (base == null) {
      base = table.getDefaultRenderer(Object.class);
    }
    final TableCellRenderer baseFinal = base;

    TableCellRenderer decorated = (tbl, value, isSelected, hasFocus, row, col) -> {
      Component c = baseFinal.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
      int modelRow = tbl.convertRowIndexToModel(row);
      if (modelRow >= 0 && modelRow < model.getRowCount()) {
        T item = model.itemAt(modelRow);
        RowIconProvider.RowIcon icon = provider.getIcon(item);
        if (c instanceof JLabel label) {
          label.setIcon(icon != null ? icon.icon() : null);
          label.setToolTipText(icon != null ? icon.tooltip() : null);
        }
      }
      return c;
    };

    column.setCellRenderer(decorated);
  }
}