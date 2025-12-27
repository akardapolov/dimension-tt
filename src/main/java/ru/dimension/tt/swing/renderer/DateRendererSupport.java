package ru.dimension.tt.swing.renderer;

import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTSchema;

public final class DateRendererSupport {
  private DateRendererSupport() {}

  private static final DateTimeFormatter DEFAULT_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

  public static <T> void installDateRenderer(
      JTable table,
      TTTableModel<T> model,
      String columnId
  ) {
    installDateRenderer(table, model, columnId, DEFAULT_FORMATTER);
  }

  public static <T> void installDateRenderer(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      DateTimeFormatter formatter
  ) {
    TTSchema<T> schema = model.schema();
    int modelIdx = schema.modelIndexOf(columnId);
    int viewIdx = table.convertColumnIndexToView(modelIdx);
    if (viewIdx < 0) return;

    TableColumn column = table.getColumnModel().getColumn(viewIdx);
    column.setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
        if (value instanceof LocalDate ld) {
          value = formatter.format(ld);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });
  }

  public static <T> void installDateRenderer(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      String pattern
  ) {
    installDateRenderer(table, model, columnId, DateTimeFormatter.ofPattern(pattern));
  }
}