package ru.dimension.tt.scan;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.data.SampleRow;

import static org.junit.jupiter.api.Assertions.*;

class TTDependencyScannerTest {

  @Test
  void scan_parsesTTColumnDefFromClassFile() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    TTScanIndex index = TTDependencyScanner.scan(cl, "ru.dimension.tt.data");

    TTScanIndex.TTRowTypeDef rowDef = index.rows().get(SampleRow.class.getName());
    assertNotNull(rowDef, "Row type must be present in index");

    // field: id
    TTScanIndex.TTMemberDef id = rowDef.members().stream()
        .filter(m -> m.kind() == TTScanIndex.MemberKind.FIELD && m.name().equals("id"))
        .findFirst().orElseThrow();

    assertEquals("id", id.column().id());
    assertEquals(0, id.column().order());
    assertEquals("ID", id.column().name());
    assertEquals(ColumnKind.NUMBER, id.column().kind());
    assertFalse(id.column().visible());
    assertFalse(id.column().editable());

    // field: name (id пустой -> fallback to member name)
    TTScanIndex.TTMemberDef name = rowDef.members().stream()
        .filter(m -> m.kind() == TTScanIndex.MemberKind.FIELD && m.name().equals("name"))
        .findFirst().orElseThrow();

    assertEquals("name", name.column().id());
    assertEquals(1, name.column().order());
    assertEquals("Name", name.column().name());
    assertEquals(ColumnKind.AUTO, name.column().kind());
    assertTrue(name.column().visible());
    assertEquals(10, name.column().minWidth());
    assertEquals(120, name.column().preferredWidth());

    // method: getLabel (editable + setter)
    TTScanIndex.TTMemberDef label = rowDef.members().stream()
        .filter(m -> m.kind() == TTScanIndex.MemberKind.METHOD && m.name().equals("getLabel"))
        .findFirst().orElseThrow();

    assertEquals("label", label.column().id());
    assertEquals(3, label.column().order());
    assertEquals("Label", label.column().name());
    assertTrue(label.column().editable());
    assertEquals("setLabel", label.column().setter());
    assertEquals(300, label.column().maxWidth());
  }
}