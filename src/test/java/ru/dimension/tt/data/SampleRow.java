package ru.dimension.tt.data;

import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.annotation.TTColumn;

public class SampleRow {

  @TTColumn(id = "id", order = 0, name = "ID", kind = ColumnKind.NUMBER, visible = false)
  private int id;

  // id не задан -> должен стать "name"
  @TTColumn(order = 1, name = "Name", minWidth = 10, preferredWidth = 120)
  private String name;

  // enum поле (для row icon)
  @TTColumn(id = "type", order = 2, name = "Type", kind = ColumnKind.TEXT)
  private DataType type;

  // метод-колонка с setter
  private String label;

  @TTColumn(id = "label", order = 3, name = "Label", editable = true, setter = "setLabel", maxWidth = 300)
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public SampleRow(int id, String name, DataType type, String label) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.label = label;
  }

  public int getId() { return id; }
  public String getName() { return name; }
  public DataType getType() { return type; }
}