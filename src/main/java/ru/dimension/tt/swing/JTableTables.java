package ru.dimension.tt.swing;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTColumnBinding;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.swing.icon.RowIconSupport;

public final class JTableTables {
  private JTableTables() {}

  public static <T> TTTable<T, JTable> create(TTRegistry registry, Class<T> rowType) {
    return create(registry, rowType, TableUi.<T>builder().build());
  }

  public static <T> TTTable<T, JTable> create(TTRegistry registry, Class<T> rowType, TableUi<T> ui) {
    TTSchema<T> schema = registry.schema(rowType);
    TTTableModel<T> model = new TTTableModel<>(schema);

    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    applyColumnWidthsAndVisibility(table, schema);

    JScrollPane sp = new JScrollPane(table);

    TTTable<T, JTable> tt = new TTTable<>(table, model, sp);

    if (ui != null && ui.rowIconProvider() != null && ui.rowIconPlacement() != null) {
      RowIconSupport.install(table, model, ui.rowIconPlacement(), ui.rowIconProvider());
    }

    return tt;
  }

  private static <T> void applyColumnWidthsAndVisibility(JTable table, TTSchema<T> schema) {
    TableColumnModel cm = table.getColumnModel();

    // В JTable “скрыть” колонку — обычно removeColumn из column model.
    // Это скелет: используем простую стратегию (без сохранения/возврата).
    for (int modelIndex = 0; modelIndex < schema.columnCount(); modelIndex++) {
      TTColumnBinding<T> c = schema.column(modelIndex);

      int viewIndex = table.convertColumnIndexToView(modelIndex);
      if (viewIndex < 0) continue;

      var col = cm.getColumn(viewIndex);

      if (c.spec().minWidth() >= 0) col.setMinWidth(c.spec().minWidth());
      if (c.spec().maxWidth() >= 0) col.setMaxWidth(c.spec().maxWidth());
      if (c.spec().preferredWidth() >= 0) col.setPreferredWidth(c.spec().preferredWidth());

      if (!c.spec().visible()) {
        cm.removeColumn(col);
      }
    }
  }
}