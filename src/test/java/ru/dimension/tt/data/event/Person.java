package ru.dimension.tt.data.event;

import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.annotation.TTColumn;

public class Person {

  @TTColumn(order = 0, name = "ID", kind = ColumnKind.NUMBER)
  private long id;

  @TTColumn(order = 1, name = "Name")
  private String name;

  @TTColumn(order = 2, name = "Status")
  private Status status;

  @TTColumn(order = 3, name = "Active", kind = ColumnKind.CHECKBOX)
  private boolean active;

  public Person() {}

  public Person(long id, String name, Status status, boolean active) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.active = active;
  }

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }

  public enum Status {
    PENDING, APPROVED, REJECTED
  }
}