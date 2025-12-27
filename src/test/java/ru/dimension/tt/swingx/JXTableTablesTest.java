package ru.dimension.tt.swingx;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.swing.TableUi;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swing.icon.IconMapper;
import ru.dimension.tt.swing.icon.RowIconProvider;
import ru.dimension.tt.swing.icon.RowIconProviders;
import ru.dimension.tt.data.DataType;
import ru.dimension.tt.data.SampleRow;

import javax.swing.Icon;
import javax.swing.JLabel;
import java.awt.Component;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JXTableTablesTest {

  static class MockIcon implements Icon {
    public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {}
    public int getIconWidth() { return 10; }
    public int getIconHeight() { return 10; }
  }

  @Test
  void create_appliesVisibilityAndWidths_toJXTable() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    TTTable<SampleRow, JXTable> tt = JXTableTables.create(reg, SampleRow.class);
    JXTable table = tt.table();

    TableColumnExt idCol = table.getColumnExt("id");
    assertNotNull(idCol);
    assertEquals("ID", idCol.getTitle());
    assertFalse(idCol.isVisible(), "ID column should be hidden via SwingX");

    int idModelIdx = tt.model().schema().modelIndexOf("id");
    assertEquals(-1, table.convertColumnIndexToView(idModelIdx));

    TableColumnExt nameCol = table.getColumnExt("name");
    assertNotNull(nameCol);
    assertEquals("Name", nameCol.getTitle());
    assertTrue(nameCol.isVisible());
    assertEquals(10, nameCol.getMinWidth());
    assertEquals(120, nameCol.getPreferredWidth());

    TableColumnExt labelCol = table.getColumnExt("label");
    assertNotNull(labelCol);
    assertEquals("Label", labelCol.getTitle());
    assertEquals(300, labelCol.getMaxWidth());
  }

  @Test
  void rowIcons_renderCorrectly_inJXTable() {
    TTRegistry reg = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    Icon mockIcon = new MockIcon();

    var schema = reg.schema(SampleRow.class);

    RowIconProvider<SampleRow> iconProvider = RowIconProviders.byEnumColumn(
        schema,
        "type",
        DataType.class,
        new IconMapper<>() {
          @Override public Icon iconFor(DataType v) { return v == DataType.NUMBER ? mockIcon : null; }
          @Override public String tooltipFor(DataType v) { return "Tip: " + v; }
        }
    );

    TTTable<SampleRow, JXTable> tt = JXTableTables.create(
        reg,
        SampleRow.class,
        TableUi.<SampleRow>builder()
            .rowIcon(iconProvider)
            .rowIconInColumn("name")
            .build()
    );

    SampleRow row = new SampleRow(1, "Test", DataType.NUMBER, "L");
    tt.setItems(List.of(row));

    int nameModelIdx = schema.modelIndexOf("name");
    int nameViewIdx = tt.table().convertColumnIndexToView(nameModelIdx);
    assertTrue(nameViewIdx >= 0);

    Component comp = tt.table().prepareRenderer(
        tt.table().getCellRenderer(0, nameViewIdx),
        0,
        nameViewIdx
    );

    assertInstanceOf(JLabel.class, comp);
    JLabel label = (JLabel) comp;

    assertSame(mockIcon, label.getIcon(), "Row icon should be present");
    assertEquals("Tip: NUMBER", label.getToolTipText());
  }
}