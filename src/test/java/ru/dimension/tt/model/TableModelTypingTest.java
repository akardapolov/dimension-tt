package ru.dimension.tt.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.Department;
import ru.dimension.tt.data.EmployeeRow;
import ru.dimension.tt.schema.TTSchema;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableModelTypingTest {

  private TTTableModel<EmployeeRow> model;

  @BeforeEach
  void setup() {
    TTRegistry registry = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();
    TTSchema<EmployeeRow> schema = registry.schema(EmployeeRow.class);
    model = new TTTableModel<>(schema);
  }

  @Test
  void columnClasses_areCorrect() {
    // Находим индексы колонок
    int idIdx = model.schema().modelIndexOf("id");
    int activeIdx = model.schema().modelIndexOf("active");
    int salaryIdx = model.schema().modelIndexOf("salary");
    int hireDateIdx = model.schema().modelIndexOf("hireDate");
    int nameIdx = model.schema().modelIndexOf("name");

    assertEquals(Integer.class, model.getColumnClass(idIdx));
    assertEquals(Boolean.class, model.getColumnClass(activeIdx));
    assertEquals(Double.class, model.getColumnClass(salaryIdx));
    assertEquals(LocalDate.class, model.getColumnClass(hireDateIdx));
    assertEquals(String.class, model.getColumnClass(nameIdx));
  }

  @Test
  void getValueAt_returnsCorrectTypes() {
    EmployeeRow row = new EmployeeRow(42, true, "John Doe", Department.ENGINEERING,
                                      75000.50, LocalDate.of(2020, 1, 15), "john@company.com", false);
    model.setItems(List.of(row));

    int idIdx = model.schema().modelIndexOf("id");
    int activeIdx = model.schema().modelIndexOf("active");
    int salaryIdx = model.schema().modelIndexOf("salary");
    int hireDateIdx = model.schema().modelIndexOf("hireDate");

    assertInstanceOf(Integer.class, model.getValueAt(0, idIdx));
    assertEquals(42, model.getValueAt(0, idIdx));

    assertInstanceOf(Boolean.class, model.getValueAt(0, activeIdx));
    assertEquals(true, model.getValueAt(0, activeIdx));

    assertInstanceOf(Double.class, model.getValueAt(0, salaryIdx));
    assertEquals(75000.50, model.getValueAt(0, salaryIdx));

    assertInstanceOf(LocalDate.class, model.getValueAt(0, hireDateIdx));
    assertEquals(LocalDate.of(2020, 1, 15), model.getValueAt(0, hireDateIdx));
  }

  @Test
  void isCellEditable_reflectsAnnotation() {
    model.setItems(List.of(new EmployeeRow(1, true, "Test", null, 0, null, null, false)));

    int activeIdx = model.schema().modelIndexOf("active");
    int salaryIdx = model.schema().modelIndexOf("salary");
    int hireDateIdx = model.schema().modelIndexOf("hireDate");
    int remoteIdx = model.schema().modelIndexOf("remote");
    int nameIdx = model.schema().modelIndexOf("name");
    int idIdx = model.schema().modelIndexOf("id");

    assertTrue(model.isCellEditable(0, activeIdx), "active should be editable");
    assertTrue(model.isCellEditable(0, salaryIdx), "salary should be editable");
    assertTrue(model.isCellEditable(0, hireDateIdx), "hireDate should be editable");
    assertTrue(model.isCellEditable(0, remoteIdx), "remote should be editable");
    assertFalse(model.isCellEditable(0, nameIdx), "name should not be editable");
    assertFalse(model.isCellEditable(0, idIdx), "id should not be editable");
  }

  @Test
  void setValueAt_updatesFieldValue() {
    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 50000, null, null, false);
    model.setItems(List.of(row));

    int activeIdx = model.schema().modelIndexOf("active");
    int salaryIdx = model.schema().modelIndexOf("salary");

    model.setValueAt(false, 0, activeIdx);
    assertFalse(row.isActive());

    model.setValueAt(75000.0, 0, salaryIdx);
    assertEquals(75000.0, row.getSalary());
  }

  @Test
  void setValueAt_updatesDateValue() {
    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 0, null, null, false);
    model.setItems(List.of(row));

    int hireDateIdx = model.schema().modelIndexOf("hireDate");
    LocalDate newDate = LocalDate.of(2023, 6, 15);

    model.setValueAt(newDate, 0, hireDateIdx);
    assertEquals(newDate, row.getHireDate());
  }
}