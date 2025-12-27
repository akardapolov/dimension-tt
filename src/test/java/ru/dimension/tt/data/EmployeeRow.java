package ru.dimension.tt.data;

import java.time.LocalDate;
import java.time.Period;
import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.annotation.TTColumn;

public class EmployeeRow {

  @TTColumn(id = "id", order = 0, name = "#", kind = ColumnKind.NUMBER, minWidth = 40, maxWidth = 60)
  private int id;

  @TTColumn(id = "active", order = 1, name = "Active", kind = ColumnKind.CHECKBOX, editable = true)
  private boolean active;

  @TTColumn(id = "name", order = 2, name = "Full Name", minWidth = 120, preferredWidth = 200)
  private String name;

  @TTColumn(id = "department", order = 3, name = "Department", preferredWidth = 120)
  private Department department;

  @TTColumn(id = "salary", order = 4, name = "Salary", kind = ColumnKind.NUMBER, preferredWidth = 100, editable = true)
  private double salary;

  @TTColumn(id = "hireDate", order = 5, name = "Hire Date", kind = ColumnKind.DATE, editable = true, preferredWidth = 120)
  private LocalDate hireDate;

  @TTColumn(id = "email", order = 6, name = "Email", preferredWidth = 180)
  private String email;

  @TTColumn(id = "remote", order = 7, name = "Remote", kind = ColumnKind.CHECKBOX, editable = true)
  private boolean remote;

  // Вычисляемая колонка через метод
  @TTColumn(id = "yearsWorked", order = 8, name = "Years", kind = ColumnKind.NUMBER, preferredWidth = 60)
  public int getYearsWorked() {
    if (hireDate == null) return 0;
    return Period.between(hireDate, LocalDate.now()).getYears();
  }

  // Вычисляемая колонка - статус
  @TTColumn(id = "status", order = 9, name = "Status", preferredWidth = 100)
  public String getStatus() {
    if (!active) return "Inactive";
    if (remote) return "Remote";
    return "Office";
  }

  public EmployeeRow() {}

  public EmployeeRow(int id, boolean active, String name, Department department,
                     double salary, LocalDate hireDate, String email, boolean remote) {
    this.id = id;
    this.active = active;
    this.name = name;
    this.department = department;
    this.salary = salary;
    this.hireDate = hireDate;
    this.email = email;
    this.remote = remote;
  }

  // Getters
  public int getId() { return id; }
  public boolean isActive() { return active; }
  public String getName() { return name; }
  public Department getDepartment() { return department; }
  public double getSalary() { return salary; }
  public LocalDate getHireDate() { return hireDate; }
  public String getEmail() { return email; }
  public boolean isRemote() { return remote; }

  // Setters для editable полей
  public void setActive(boolean active) { this.active = active; }
  public void setSalary(double salary) { this.salary = salary; }
  public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
  public void setRemote(boolean remote) { this.remote = remote; }

  public static EmployeeRow of(int id, boolean active, String name, Department dept,
                               double salary, LocalDate hireDate, String email, boolean remote) {
    return new EmployeeRow(id, active, name, dept, salary, hireDate, email, remote);
  }
}