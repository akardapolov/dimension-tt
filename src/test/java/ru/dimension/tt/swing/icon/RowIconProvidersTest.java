package ru.dimension.tt.swing.icon;

import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swing.TableUi;
import ru.dimension.tt.data.DataType;
import ru.dimension.tt.data.SampleRow;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import java.awt.Component;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RowIconProvidersTest {

  static final class DummyIcon implements Icon {
    private final String id;
    DummyIcon(String id) { this.id = id; }
    public int getIconWidth() { return 1; }
    public int getIconHeight() { return 1; }
    public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {}
    @Override public String toString() { return "DummyIcon(" + id + ")"; }
  }

  @Test
  void rowIconProvider_andRenderer_work() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    Icon n123 = new DummyIcon("123");
    Icon abc = new DummyIcon("abc");

    IconMapper<DataType> mapper = new IconMapper<>() {
      @Override
      public Icon iconFor(DataType value) {
        return switch (value) {
          case NUMBER -> n123;
          case STRING -> abc;
          default -> null;
        };
      }

      @Override
      public String tooltipFor(DataType value) {
        return "Type: " + value;
      }
    };

    var schema = reg.schema(SampleRow.class);
    RowIconProvider<SampleRow> provider =
        RowIconProviders.byEnumColumn(schema, "type", DataType.class, mapper);

    TTTable<SampleRow, JTable> tt = JTableTables.create(
        reg,
        SampleRow.class,
        TableUi.<SampleRow>builder()
            .rowIcon(provider)
            .rowIconInColumn("name")
            .build()
    );

    SampleRow row = new SampleRow(1, "Row-1", DataType.NUMBER, "L");
    tt.setItems(List.of(row));

    int nameModelCol = tt.model().schema().modelIndexOf("name");
    int nameViewCol = tt.table().convertColumnIndexToView(nameModelCol);
    assertTrue(nameViewCol >= 0);

    var renderer = tt.table().getColumnModel().getColumn(nameViewCol).getCellRenderer();
    assertNotNull(renderer);

    Component c = renderer.getTableCellRendererComponent(
        tt.table(),
        tt.model().getValueAt(0, nameModelCol),
        false,
        false,
        0,
        nameViewCol
    );

    assertInstanceOf(JLabel.class, c);
    JLabel label = (JLabel) c;

    assertSame(n123, label.getIcon());
    assertEquals("Type: NUMBER", label.getToolTipText());
  }
}