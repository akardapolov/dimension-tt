package ru.dimension.tt.swing.renderer;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTSchema;

public final class NumberRendererSupport {
  private NumberRendererSupport() {}

  public static <T> void installCurrencyRenderer(
      JTable table,
      TTTableModel<T> model,
      String columnId
  ) {
    installNumberRenderer(table, model, columnId, NumberFormat.getCurrencyInstance());
  }

  public static <T> void installNumberRenderer(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      NumberFormat format
  ) {
    TTSchema<T> schema = model.schema();
    int modelIdx = schema.modelIndexOf(columnId);
    int viewIdx = table.convertColumnIndexToView(modelIdx);
    if (viewIdx < 0) return;

    TableColumn column = table.getColumnModel().getColumn(viewIdx);
    column.setCellRenderer(new DefaultTableCellRenderer() {
      {
        setHorizontalAlignment(SwingConstants.RIGHT);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
        if (value instanceof Number n) {
          value = format.format(n);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });
  }
}