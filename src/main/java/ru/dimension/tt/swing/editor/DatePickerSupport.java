package ru.dimension.tt.swing.editor;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.util.function.Supplier;
import javax.swing.JTable;
import ru.dimension.tt.model.TTTableModel;

public final class DatePickerSupport {
  private DatePickerSupport() {}

  /**
   * Устанавливает LGoodDatePicker как редактор для колонки с датой.
   * Использует настройки по умолчанию.
   */
  public static <T> void installDatePicker(
      JTable table,
      TTTableModel<T> model,
      String columnId
  ) {
    installDatePicker(table, model, columnId, DatePickerSupport::createDefaultSettings);
  }

  /**
   * Устанавливает LGoodDatePicker как редактор для колонки с датой.
   * Принимает supplier для создания новых settings при каждом редактировании.
   *
   * @param settingsSupplier фабрика настроек (вызывается при каждом создании DatePicker)
   */
  public static <T> void installDatePicker(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      Supplier<DatePickerSettings> settingsSupplier
  ) {
    CellEditorSupport.installDateEditor(
        table,
        model,
        columnId,
        () -> new DatePicker(settingsSupplier.get()),  // Каждый раз новые settings!
        DatePicker::getDate,
        DatePicker::setDate
    );
  }

  /**
   * Устанавливает LGoodDatePicker с кастомным конфигуратором настроек.
   * Удобно для сложной настройки.
   *
   * @param settingsConfigurer функция конфигурации, вызывается для новых settings
   */
  public static <T> void installDatePicker(
      JTable table,
      TTTableModel<T> model,
      String columnId,
      java.util.function.Consumer<DatePickerSettings> settingsConfigurer
  ) {
    installDatePicker(table, model, columnId, () -> {
      DatePickerSettings settings = createDefaultSettings();
      settingsConfigurer.accept(settings);
      return settings;
    });
  }

  /**
   * Создаёт настройки DatePicker по умолчанию.
   */
  public static DatePickerSettings createDefaultSettings() {
    DatePickerSettings settings = new DatePickerSettings();
    settings.setAllowEmptyDates(true);
    settings.setAllowKeyboardEditing(true);
    return settings;
  }
}