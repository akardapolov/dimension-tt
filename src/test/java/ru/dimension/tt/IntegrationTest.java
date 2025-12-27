package ru.dimension.tt;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.DataType;
import ru.dimension.tt.data.Department;
import ru.dimension.tt.data.EmployeeRow;
import ru.dimension.tt.data.SampleRow;
import ru.dimension.tt.data.TestDataGenerator;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;

import javax.swing.JTable;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

  @Test
  void fullWorkflow_scanBuildDisplay() {
    // 1. Создаём registry со сканированием
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    // 2. Проверяем, что типы найдены
    assertTrue(reg.indexedRowTypes().contains(EmployeeRow.class.getName()));
    assertTrue(reg.indexedRowTypes().contains(SampleRow.class.getName()));

    // 3. Получаем схему
    TTSchema<EmployeeRow> schema = reg.schema(EmployeeRow.class);
    assertEquals(10, schema.columnCount()); // id, active, name, department, salary, hireDate, email, remote, yearsWorked, status

    // 4. Создаём таблицу
    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);

    // 5. Загружаем данные
    List<EmployeeRow> data = TestDataGenerator.generateEmployees(10);
    tt.setItems(data);

    // 6. Проверяем данные в модели
    assertEquals(10, tt.model().getRowCount());

    // 7. Проверяем значения
    EmployeeRow first = tt.model().itemAt(0);
    assertNotNull(first.getName());
    assertNotNull(first.getDepartment());
  }

  @Test
  void multipleRowTypes_workIndependently() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> empTable = JTableTables.create(reg, EmployeeRow.class);
    TTTable<SampleRow, JTable> sampleTable = JTableTables.create(reg, SampleRow.class);

    empTable.setItems(TestDataGenerator.generateEmployees(5));
    sampleTable.setItems(TestDataGenerator.generateSampleRows(5));

    assertEquals(5, empTable.model().getRowCount());
    assertEquals(5, sampleTable.model().getRowCount());

    // Схемы разные
    assertNotEquals(
        empTable.model().schema().columnCount(),
        sampleTable.model().schema().columnCount()
    );
  }

  @Test
  void editing_persistsChanges() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);

    EmployeeRow row = new EmployeeRow(1, true, "Original", Department.HR,
                                      50000, LocalDate.of(2020, 1, 1), "test@test.com", false);
    tt.setItems(List.of(row));

    // Редактируем через модель
    int salaryIdx = tt.model().schema().modelIndexOf("salary");
    tt.model().setValueAt(75000.0, 0, salaryIdx);

    // Проверяем, что оригинальный объект изменился
    assertEquals(75000.0, row.getSalary());

    // Проверяем через itemAt
    assertEquals(75000.0, tt.model().itemAt(0).getSalary());
  }

  @Test
  void selection_returnsCorrectItem() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<EmployeeRow, JTable> tt = JTableTables.create(reg, EmployeeRow.class);
    tt.setItems(TestDataGenerator.generateEmployees(5));

    // Нет выбора
    assertTrue(tt.selectedItem().isEmpty());

    // Выбираем строку
    tt.table().setRowSelectionInterval(2, 2);
    assertTrue(tt.selectedItem().isPresent());
    assertEquals(tt.model().itemAt(2), tt.selectedItem().get());
  }
}