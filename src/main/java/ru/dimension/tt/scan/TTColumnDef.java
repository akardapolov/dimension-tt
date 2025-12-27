package ru.dimension.tt.scan;

public record TTColumnDef(
    String id,
    int order,
    String name,
    String kindName, // Храним имя константы (напр. "NUMBER"), чтобы не грузить Enum класс
    boolean visible,
    boolean editable,
    int minWidth,
    int maxWidth,
    int preferredWidth,
    String setter
) {
  // Дефолтные значения (соответствуют @TTColumn)
  public static final TTColumnDef DEFAULT = new TTColumnDef(
      "", 0, "", "AUTO", true, false, -1, -1, -1, ""
  );
}