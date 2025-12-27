package ru.dimension.tt.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import org.jdesktop.swingx.JXTable;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.data.DataType;
import ru.dimension.tt.data.Department;
import ru.dimension.tt.data.EmployeeRow;
import ru.dimension.tt.data.SampleRow;
import ru.dimension.tt.data.TestDataGenerator;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swing.TableUi;
import ru.dimension.tt.swing.editor.DatePickerSupport;
import ru.dimension.tt.swing.icon.IconMapper;
import ru.dimension.tt.swing.icon.RowIconProvider;
import ru.dimension.tt.swing.icon.RowIconProviders;
import ru.dimension.tt.swing.renderer.DateRendererSupport;
import ru.dimension.tt.swing.renderer.NumberRendererSupport;
import ru.dimension.tt.swingx.JXTableTables;

public class TTDemoMain {

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    SwingUtilities.invokeLater(TTDemoMain::createAndShowGUI);
  }

  private static void createAndShowGUI() {
    JFrame frame = new JFrame("Dimension TT Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(1200, 600));

    TTRegistry registry = TT.builder()
        .scanPackages("ru.dimension.tt.data")
        .build();

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Employees (JXTable)", createEmployeePanel(registry));
    tabs.addTab("Simple Data (JTable)", createSimplePanel(registry));
    tabs.addTab("Editable Demo", createEditablePanel(registry));

    frame.add(tabs, BorderLayout.CENTER);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private static JPanel createEmployeePanel(TTRegistry registry) {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Иконки для департаментов
    IconMapper<Department> deptIcons = new IconMapper<>() {
      @Override
      public Icon iconFor(Department value) {
        return new DepartmentIcon(getDeptColor(value));
      }

      @Override
      public String tooltipFor(Department value) {
        return value.displayName() + " " + value.emoji();
      }

      private Color getDeptColor(Department d) {
        return switch (d) {
          case ENGINEERING -> new Color(0x4CAF50);
          case SALES -> new Color(0x2196F3);
          case MARKETING -> new Color(0xFF9800);
          case HR -> new Color(0x9C27B0);
          case FINANCE -> new Color(0x607D8B);
        };
      }
    };

    var schema = registry.schema(EmployeeRow.class);
    RowIconProvider<EmployeeRow> iconProvider =
        RowIconProviders.byEnumColumn(schema, "department", Department.class, deptIcons);

    TTTable<EmployeeRow, JXTable> tt = JXTableTables.create(
        registry,
        EmployeeRow.class,
        TableUi.<EmployeeRow>builder()
            .rowIcon(iconProvider)
            .rowIconInColumn("name")
            .build()
    );

    enlargeTableFont(tt.table(), 2.0f);

    // Устанавливаем кастомные рендереры
    DateRendererSupport.installDateRenderer(tt.table(), tt.model(), "hireDate", "dd.MM.yyyy");
    NumberRendererSupport.installCurrencyRenderer(tt.table(), tt.model(), "salary");

    // Устанавливаем DatePicker для редактирования даты
    DatePickerSupport.installDatePicker(tt.table(), tt.model(), "hireDate", settings -> {
      settings.setAllowEmptyDates(false);
    });

    // Загружаем данные
    List<EmployeeRow> employees = TestDataGenerator.generateEmployees(10);
    tt.setItems(employees);

    panel.add(tt.scrollPane(), BorderLayout.CENTER);
    return panel;
  }

  private static JPanel createSimplePanel(TTRegistry registry) {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    IconMapper<DataType> typeIcons = new IconMapper<>() {
      @Override
      public Icon iconFor(DataType value) {
        return new TypeIcon(value);
      }

      @Override
      public String tooltipFor(DataType value) {
        return "Data type: " + value.name();
      }
    };

    var schema = registry.schema(SampleRow.class);
    RowIconProvider<SampleRow> iconProvider =
        RowIconProviders.byEnumColumn(schema, "type", DataType.class, typeIcons);

    TTTable<SampleRow, javax.swing.JTable> tt = JTableTables.create(
        registry,
        SampleRow.class,
        TableUi.<SampleRow>builder()
            .rowIcon(iconProvider)
            .rowIconInColumn("name")
            .build()
    );

    enlargeTableFont(tt.table(), 2.0f);

    tt.setItems(TestDataGenerator.generateSampleRows(10));

    panel.add(tt.scrollPane(), BorderLayout.CENTER);
    return panel;
  }

  private static JPanel createEditablePanel(TTRegistry registry) {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    TTTable<EmployeeRow, JXTable> tt = JXTableTables.create(registry, EmployeeRow.class);

    enlargeTableFont(tt.table(), 2.0f);

    DateRendererSupport.installDateRenderer(tt.table(), tt.model(), "hireDate");
    NumberRendererSupport.installCurrencyRenderer(tt.table(), tt.model(), "salary");
    DatePickerSupport.installDatePicker(tt.table(), tt.model(), "hireDate");

    List<EmployeeRow> employees = TestDataGenerator.generateEmployees(10);
    tt.setItems(employees);

    // Включаем сортировку для демонстрации
    tt.table().setSortable(true);

    panel.add(tt.scrollPane(), BorderLayout.CENTER);
    return panel;
  }

  // Простая иконка для департамента
  static class DepartmentIcon implements Icon {
    private final Color color;

    DepartmentIcon(Color color) {
      this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(color);
      g.fillOval(x + 1, y + 1, 12, 12);
      g.setColor(color.darker());
      g.drawOval(x + 1, y + 1, 12, 12);
    }

    @Override
    public int getIconWidth() { return 14; }

    @Override
    public int getIconHeight() { return 14; }
  }

  // Иконка для типа данных
  static class TypeIcon implements Icon {
    private final DataType type;

    TypeIcon(DataType type) {
      this.type = type;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Color color = switch (type) {
        case NUMBER -> Color.BLUE;
        case STRING -> Color.GREEN;
        case DATE -> Color.ORANGE;
        case BOOLEAN -> Color.MAGENTA;
      };

      g.setColor(color);
      switch (type) {
        case NUMBER -> {
          g.fillRect(x + 2, y + 2, 10, 10);
        }
        case STRING -> {
          g.fillRoundRect(x + 2, y + 2, 10, 10, 4, 4);
        }
        case DATE -> {
          int[] xPoints = {x + 7, x + 12, x + 2};
          int[] yPoints = {y + 2, y + 12, y + 12};
          g.fillPolygon(xPoints, yPoints, 3);
        }
        case BOOLEAN -> {
          g.fillOval(x + 2, y + 2, 10, 10);
        }
      }
    }

    @Override
    public int getIconWidth() { return 14; }

    @Override
    public int getIconHeight() { return 14; }
  }

  private static void enlargeTableFont(JTable table, float factor) {
    // Шрифт ячеек
    Font original = table.getFont();
    Font larger = original.deriveFont(original.getSize() * factor);
    table.setFont(larger);

    // Высота строк
    table.setRowHeight((int) (table.getRowHeight() * factor));

    // Шрифт заголовков
    JTableHeader header = table.getTableHeader();
    Font headerFont = header.getFont().deriveFont(header.getFont().getSize() * factor);
    header.setFont(headerFont);
  }
}