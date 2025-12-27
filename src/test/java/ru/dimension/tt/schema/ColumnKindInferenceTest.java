package ru.dimension.tt.schema;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.EmployeeRow;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ColumnKindInferenceTest {

  private static TTRegistry registry;
  private static TTSchema<EmployeeRow> schema;

  @BeforeAll
  static void setup() {
    registry = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();
    schema = registry.schema(EmployeeRow.class);
  }

  @Test
  void checkboxKind_mapsToBoolean() {
    TTColumnBinding<EmployeeRow> activeCol = findColumn("active");
    assertEquals(ColumnKind.CHECKBOX, activeCol.spec().kind());
    assertEquals(Boolean.class, activeCol.columnClass());
  }

  @Test
  void numberKind_mapsToCorrectType() {
    TTColumnBinding<EmployeeRow> idCol = findColumn("id");
    assertEquals(ColumnKind.NUMBER, idCol.spec().kind());
    assertEquals(Integer.class, idCol.columnClass());

    TTColumnBinding<EmployeeRow> salaryCol = findColumn("salary");
    assertEquals(ColumnKind.NUMBER, salaryCol.spec().kind());
    assertEquals(Double.class, salaryCol.columnClass());
  }

  @Test
  void dateKind_mapsToLocalDate() {
    TTColumnBinding<EmployeeRow> hireDateCol = findColumn("hireDate");
    assertEquals(ColumnKind.DATE, hireDateCol.spec().kind());
    assertEquals(LocalDate.class, hireDateCol.columnClass());
  }

  @Test
  void autoKind_infersText_forString() {
    TTColumnBinding<EmployeeRow> nameCol = findColumn("name");
    assertEquals(ColumnKind.TEXT, nameCol.spec().kind());
    assertEquals(String.class, nameCol.columnClass());
  }

  @Test
  void methodColumn_infersCorrectly() {
    TTColumnBinding<EmployeeRow> yearsCol = findColumn("yearsWorked");
    assertNotNull(yearsCol);
    assertEquals(ColumnKind.NUMBER, yearsCol.spec().kind());
    assertEquals(Integer.class, yearsCol.columnClass());

    // Проверяем, что getter работает
    EmployeeRow row = new EmployeeRow(1, true, "Test", null, 50000,
                                      LocalDate.now().minusYears(5), "test@test.com", false);
    Object value = yearsCol.getter().apply(row);
    assertEquals(5, value);
  }

  @Test
  void computedStatusColumn_works() {
    TTColumnBinding<EmployeeRow> statusCol = findColumn("status");
    assertNotNull(statusCol);

    EmployeeRow activeRemote = new EmployeeRow(1, true, "Test", null, 0, null, null, true);
    assertEquals("Remote", statusCol.getter().apply(activeRemote));

    EmployeeRow activeOffice = new EmployeeRow(2, true, "Test", null, 0, null, null, false);
    assertEquals("Office", statusCol.getter().apply(activeOffice));

    EmployeeRow inactive = new EmployeeRow(3, false, "Test", null, 0, null, null, false);
    assertEquals("Inactive", statusCol.getter().apply(inactive));
  }

  private TTColumnBinding<EmployeeRow> findColumn(String id) {
    int idx = schema.modelIndexOf(id);
    return schema.column(idx);
  }
}