package ru.dimension.tt.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TestDataGenerator {
  private TestDataGenerator() {}

  private static final String[] FIRST_NAMES = {
      "Alice", "Bob", "Charlie", "Diana", "Edward",
      "Fiona", "George", "Hannah", "Ivan", "Julia"
  };

  private static final String[] LAST_NAMES = {
      "Smith", "Johnson", "Williams", "Brown", "Jones",
      "Miller", "Davis", "Wilson", "Taylor", "Anderson"
  };

  public static List<EmployeeRow> generateEmployees(int count) {
    return generateEmployees(count, new Random(42));
  }

  public static List<EmployeeRow> generateEmployees(int count, Random random) {
    List<EmployeeRow> employees = new ArrayList<>(count);
    Department[] departments = Department.values();

    for (int i = 1; i <= count; i++) {
      String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
      String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
      String name = firstName + " " + lastName;
      String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@company.com";

      Department dept = departments[random.nextInt(departments.length)];
      double salary = 50_000 + random.nextInt(100_000);
      LocalDate hireDate = LocalDate.now()
          .minusYears(random.nextInt(15))
          .minusMonths(random.nextInt(12))
          .minusDays(random.nextInt(28));

      boolean active = random.nextDouble() > 0.1;
      boolean remote = random.nextBoolean();

      employees.add(new EmployeeRow(i, active, name, dept, salary, hireDate, email, remote));
    }

    return employees;
  }

  public static List<SampleRow> generateSampleRows(int count) {
    List<SampleRow> rows = new ArrayList<>(count);
    DataType[] types = DataType.values();
    Random random = new Random(42);

    for (int i = 1; i <= count; i++) {
      DataType type = types[random.nextInt(types.length)];
      rows.add(new SampleRow(i, "Item-" + i, type, "Label-" + (char)('A' + i % 26)));
    }

    return rows;
  }
}