package ru.dimension.tt.swing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.swing.event.RowActions;
import ru.dimension.tt.data.event.Person;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableUi with RowActions Integration Tests")
class TableUiRowActionsTest {

  private TTRegistry registry;

  @BeforeEach
  void setUp() {
    registry = TT.builder()
        .scanPackages("ru.dimension.tt.test.model")
        .build();
  }

  @Test
  @DisplayName("TableUi builder convenience methods")
  void tableUiBuilderConvenienceMethods() throws Exception {
    AtomicReference<Person> doubleClicked = new AtomicReference<>();
    AtomicReference<Person> selected = new AtomicReference<>();
    AtomicReference<Person> enterPressed = new AtomicReference<>();

    SwingUtilities.invokeAndWait(() -> {
      TableUi<Person> ui = TableUi.<Person>builder()
          .onDoubleClick(doubleClicked::set)
          .onSelectionChanged(selected::set)
          .onEnterKey(enterPressed::set)
          .build();

      assertNotNull(ui.rowActions());
      assertNotNull(ui.rowActions().onDoubleClick());
      assertNotNull(ui.rowActions().onSelectionChanged());
      assertNotNull(ui.rowActions().onRowEnterKey());
    });
  }

  @Test
  @DisplayName("TableUi with explicit RowActions")
  void tableUiWithExplicitRowActions() throws Exception {
    RowActions<Person> actions = RowActions.<Person>builder()
        .onDoubleClickRow(p -> System.out.println("Double clicked: " + p.getName()))
        .contextMenuForRow(p -> {
          JPopupMenu menu = new JPopupMenu();
          menu.add(new JMenuItem("Edit " + p.getName()));
          return menu;
        })
        .build();

    SwingUtilities.invokeAndWait(() -> {
      TableUi<Person> ui = TableUi.<Person>builder()
          .rowActions(actions)
          .build();

      assertSame(actions, ui.rowActions());
    });
  }

  @Test
  @DisplayName("Create table with TableUi containing row actions")
  void createTableWithRowActions() throws Exception {
    AtomicReference<Person> doubleClicked = new AtomicReference<>();

    SwingUtilities.invokeAndWait(() -> {
      TableUi<Person> ui = TableUi.<Person>builder()
          .onDoubleClick(doubleClicked::set)
          .build();

      TTTable<Person, JTable> tt = JTableTables.create(registry, Person.class, ui);

      assertNotNull(tt);
      assertNotNull(tt.table());
      assertNotNull(tt.model());

      // Verify mouse listeners were installed
      assertTrue(tt.table().getMouseListeners().length > 0);
    });
  }

  @Test
  @DisplayName("Full usage example from documentation")
  void fullUsageExample() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      // This mirrors the usage example from the documentation
      TTTable<Person, JTable> tt = JTableTables.create(registry, Person.class,
                                                       TableUi.<Person>builder()
                                                           .onDoubleClick(person -> {
                                                             // Would call openPersonDialog(person) in real code
                                                             assertNotNull(person);
                                                           })
                                                           .onSelectionChanged(person -> {
                                                             // Would call updateDetailsPanel(person) in real code
                                                           })
                                                           .onEnterKey(person -> {
                                                             // Would call editPerson(person) in real code
                                                             assertNotNull(person);
                                                           })
                                                           .contextMenu(person -> {
                                                             JPopupMenu menu = new JPopupMenu();
                                                             menu.add(new JMenuItem("Edit"));
                                                             menu.add(new JMenuItem("Delete"));
                                                             return menu;
                                                           })
                                                           .build()
      );

      tt.setItems(List.of(
          new Person(1L, "Alice", Person.Status.APPROVED, true),
          new Person(2L, "Bob", Person.Status.PENDING, false)
      ));

      assertEquals(2, tt.model().getRowCount());
    });
  }
}