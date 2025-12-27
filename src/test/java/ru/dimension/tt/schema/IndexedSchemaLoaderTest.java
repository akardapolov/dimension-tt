package ru.dimension.tt.schema;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.SampleRow;

import static org.junit.jupiter.api.Assertions.*;

class IndexedSchemaLoaderTest {

  @Test
  void schema_loadsFromIndex_withoutFallback() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .fallbackSchemaLoader(rowType -> { throw new AssertionError("..."); })
        .build();

    TTSchema<SampleRow> schema = reg.schema(SampleRow.class);
    assertEquals(4, schema.columnCount());

    // Проверяем порядок (order) и значения из индекса
    assertEquals("id", schema.column(0).spec().id());
    assertEquals("ID", schema.column(0).spec().name());
    assertFalse(schema.column(0).spec().visible());

    assertEquals("name", schema.column(1).spec().id());
    assertEquals("Name", schema.column(1).spec().name());
    assertEquals(10, schema.column(1).spec().minWidth());
    assertEquals(120, schema.column(1).spec().preferredWidth());

    assertEquals("type", schema.column(2).spec().id());
    assertEquals("Type", schema.column(2).spec().name());

    assertEquals("label", schema.column(3).spec().id());
    assertTrue(schema.column(3).spec().editable());
    assertEquals(300, schema.column(3).spec().maxWidth());
  }
}