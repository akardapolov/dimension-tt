package ru.dimension.tt.swing.renderer;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.EmployeeRow;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenderersTest {

  @Test
  void dateRenderer_formatsLocalDate() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);
    JTable table = tt.table();

    DateRendererSupport.installDateRenderer(table, tt.model(), "hireDate", "dd.MM.yyyy");

    LocalDate testDate = LocalDate.of(2020, 3, 15);
    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 0, testDate, null, false);
    tt.setItems(List.of(row));

    int hireDateModelIdx = tt.model().schema().modelIndexOf("hireDate");
    int hireDateViewIdx = table.convertColumnIndexToView(hireDateModelIdx);

    TableCellRenderer renderer = table.getColumnModel()
        .getColumn(hireDateViewIdx).getCellRenderer();
    assertNotNull(renderer);

    Component comp = renderer.getTableCellRendererComponent(
        table, testDate, false, false, 0, hireDateViewIdx);

    assertInstanceOf(JLabel.class, comp);
    JLabel label = (JLabel) comp;
    assertEquals("15.03.2020", label.getText());
  }

  @Test
  void currencyRenderer_formatsNumber() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);
    JTable table = tt.table();

    NumberRendererSupport.installCurrencyRenderer(table, tt.model(), "salary");

    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 75000.50, null, null, false);
    tt.setItems(List.of(row));

    int salaryModelIdx = tt.model().schema().modelIndexOf("salary");
    int salaryViewIdx = table.convertColumnIndexToView(salaryModelIdx);

    TableCellRenderer renderer = table.getColumnModel()
        .getColumn(salaryViewIdx).getCellRenderer();
    assertNotNull(renderer);

    Component comp = renderer.getTableCellRendererComponent(
        table, 75000.50, false, false, 0, salaryViewIdx);

    assertInstanceOf(JLabel.class, comp);
    JLabel label = (JLabel) comp;

    // Формат зависит от локали, проверяем что число отформатировано
    assertNotEquals("75000.5", label.getText());
    assertTrue(label.getText().contains("75"));
  }
}