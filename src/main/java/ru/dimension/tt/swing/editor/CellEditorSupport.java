package ru.dimension.tt.swing.editor;

import java.awt.Component;
import java.time.LocalDate;
import java.util.EventObject;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTSchema;

public final class CellEditorSupport {
  private CellEditorSupport() {}

  /**
   * Устанавливает кастомный редактор для колонки по её id.
   */
  public static <T> void installEditor(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      TableCellEditor editor
  ) {
    TTSchema<T> schema = model.schema();
    int modelIdx = schema.modelIndexOf(columnId);
    int viewIdx = table.convertColumnIndexToView(modelIdx);
    if (viewIdx < 0) return;

    TableColumn column = table.getColumnModel().getColumn(viewIdx);
    column.setCellEditor(editor);
  }

  /**
   * Устанавливает кастомный редактор для колонки с LocalDate.
   * Принимает supplier компонента-редактора и функции для get/set значения.
   */
  public static <T, C extends Component> void installDateEditor(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      Supplier<C> componentSupplier,
      Function<C, LocalDate> valueGetter,
      BiConsumer<C, LocalDate> valueSetter
  ) {
    TableCellEditor editor = new LocalDateCellEditor<>(componentSupplier, valueGetter, valueSetter);
    installEditor(table, model, columnId, editor);
  }

  /**
   * Универсальный редактор для LocalDate, использующий произвольный компонент.
   */
  private static class LocalDateCellEditor<C extends Component> extends AbstractCellEditor implements TableCellEditor {
    private final Supplier<C> componentSupplier;
    private final Function<C, LocalDate> valueGetter;
    private final BiConsumer<C, LocalDate> valueSetter;
    private C component;

    LocalDateCellEditor(Supplier<C> componentSupplier,
                        Function<C, LocalDate> valueGetter,
                        BiConsumer<C, LocalDate> valueSetter) {
      this.componentSupplier = componentSupplier;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
      component = componentSupplier.get();
      if (value instanceof LocalDate ld) {
        valueSetter.accept(component, ld);
      } else {
        valueSetter.accept(component, null);
      }
      return component;
    }

    @Override
    public Object getCellEditorValue() {
      return component != null ? valueGetter.apply(component) : null;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
      return true;
    }
  }

  /**
   * Создаёт generic редактор для любого типа значения.
   */
  public static <T, C extends Component, V> void installGenericEditor(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      Class<V> valueType,
      Supplier<C> componentSupplier,
      Function<C, V> valueGetter,
      BiConsumer<C, V> valueSetter
  ) {
    TableCellEditor editor = new GenericCellEditor<>(valueType, componentSupplier, valueGetter, valueSetter);
    installEditor(table, model, columnId, editor);
  }

  /**
   * Универсальный редактор для произвольного типа.
   */
  private static class GenericCellEditor<C extends Component, V> extends AbstractCellEditor implements TableCellEditor {
    private final Class<V> valueType;
    private final Supplier<C> componentSupplier;
    private final Function<C, V> valueGetter;
    private final BiConsumer<C, V> valueSetter;
    private C component;

    GenericCellEditor(Class<V> valueType,
                      Supplier<C> componentSupplier,
                      Function<C, V> valueGetter,
                      BiConsumer<C, V> valueSetter) {
      this.valueType = valueType;
      this.componentSupplier = componentSupplier;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
      component = componentSupplier.get();
      if (valueType.isInstance(value)) {
        valueSetter.accept(component, valueType.cast(value));
      } else {
        valueSetter.accept(component, null);
      }
      return component;
    }

    @Override
    public Object getCellEditorValue() {
      return component != null ? valueGetter.apply(component) : null;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
      return true;
    }
  }
}