package ru.dimension.tt.scan;

import java.util.List;
import java.util.Map;
import ru.dimension.tt.annotation.ColumnKind;

public record TTScanIndex(Map<String, TTRowTypeDef> rows) {

  public record TTRowTypeDef(String className, List<TTMemberDef> members) {}

  public record TTMemberDef(MemberKind kind, String name, TTColumnDef column) {}

  public record TTColumnDef(
      String id,              // resolved id (если в аннотации пусто -> имя поля/метода)
      int order,              // required
      String name,            // required
      ColumnKind kind,        // may be AUTO
      boolean visible,
      boolean editable,
      int minWidth,
      int maxWidth,
      int preferredWidth,
      String setter
  ) {}

  public enum MemberKind { FIELD, METHOD }
}