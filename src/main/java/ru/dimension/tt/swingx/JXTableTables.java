package ru.dimension.tt.swingx;

import javax.swing.*;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTColumnBinding;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swing.TableUi;
import ru.dimension.tt.swing.event.RowEventSupport;
import ru.dimension.tt.swing.icon.RowIconSupport;

public final class JXTableTables {
  private JXTableTables() {}

  public static <T> TTTable<T, JXTable> create(TTRegistry registry, Class<T> rowType) {
    return create(registry, rowType, TableUi.<T>builder().build());
  }

  public static <T> TTTable<T, JXTable> create(TTRegistry registry, Class<T> rowType, TableUi<T> ui) {
    TTSchema<T> schema = registry.schema(rowType);
    TTTableModel<T> model = new TTTableModel<>(schema);

    JXTable table = new JXTable(model);
    table.setColumnControlVisible(true);
    table.setHorizontalScrollEnabled(true);
    table.setSortable(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    applySwingXVisibilityAndWidths(table, schema);

    JScrollPane sp = new JScrollPane(table);

    TTTable<T, JXTable> tt = new TTTable<>(table, model, sp);

    if (ui != null) {
      if (ui.rowIconProvider() != null && ui.rowIconPlacement() != null) {
        RowIconSupport.install(table, model, ui.rowIconPlacement(), ui.rowIconProvider());
      }

      if (ui.rowActions() != null) {
        RowEventSupport.install(table, model, ui.rowActions());
      }
    }

    return tt;
  }

  private static <T> void applySwingXVisibilityAndWidths(JXTable table, TTSchema<T> schema) {
    // Pass 1: title/identifier/header + widths (пока все колонки вьюшные существуют)
    for (int modelIndex = 0; modelIndex < schema.columnCount(); modelIndex++) {
      int viewIndex = table.convertColumnIndexToView(modelIndex);
      if (viewIndex < 0) continue;

      TTColumnBinding<T> c = schema.column(modelIndex);
      var spec = c.spec();

      TableColumnExt ext = table.getColumnExt(viewIndex);

      // Стабильный доступ по id даже если колонка станет hidden
      ext.setIdentifier(spec.id());

      // UI title/header
      ext.setTitle(spec.name());
      ext.setHeaderValue(spec.name());

      if (spec.minWidth() >= 0) ext.setMinWidth(spec.minWidth());
      if (spec.maxWidth() >= 0) ext.setMaxWidth(spec.maxWidth());
      if (spec.preferredWidth() >= 0) ext.setPreferredWidth(spec.preferredWidth());
    }

    // Pass 2: visibility (после каждого скрытия view-индексы меняются -> каждый раз пересчитываем)
    for (int modelIndex = 0; modelIndex < schema.columnCount(); modelIndex++) {
      int viewIndex = table.convertColumnIndexToView(modelIndex);
      if (viewIndex < 0) continue;

      var spec = schema.column(modelIndex).spec();
      table.getColumnExt(viewIndex).setVisible(spec.visible());
    }
  }
}