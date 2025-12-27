package ru.dimension.tt.swing;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.Department;
import ru.dimension.tt.data.EmployeeRow;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckboxEditingTest {

  @Test
  void checkboxColumn_usesCheckboxEditor() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);
    JTable table = tt.table();

    EmployeeRow row = new EmployeeRow(1, true, "Test", Department.ENGINEERING,
                                      50000, LocalDate.now(), "test@test.com", false);
    tt.setItems(List.of(row));

    int activeModelIdx = tt.model().schema().modelIndexOf("active");
    int activeViewIdx = table.convertColumnIndexToView(activeModelIdx);

    // JTable должен использовать checkbox для Boolean
    Class<?> colClass = tt.model().getColumnClass(activeModelIdx);
    assertEquals(Boolean.class, colClass);

    // Проверяем, что редактор для Boolean возвращает JCheckBox
    TableCellEditor editor = table.getDefaultEditor(Boolean.class);
    assertNotNull(editor);

    var comp = editor.getTableCellEditorComponent(table, true, true, 0, activeViewIdx);
    assertInstanceOf(JCheckBox.class, comp);
  }

  @Test
  void checkboxEditing_togglesValue() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);

    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 0, null, null, false);
    tt.setItems(List.of(row));

    int activeIdx = tt.model().schema().modelIndexOf("active");
    int remoteIdx = tt.model().schema().modelIndexOf("remote");

    // Изначально active=true, remote=false
    assertTrue(row.isActive());
    assertFalse(row.isRemote());

    // Меняем через модель
    tt.model().setValueAt(false, 0, activeIdx);
    tt.model().setValueAt(true, 0, remoteIdx);

    assertFalse(row.isActive());
    assertTrue(row.isRemote());
  }
}