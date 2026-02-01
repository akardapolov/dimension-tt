package ru.dimension.tt.swing.event;

import org.junit.jupiter.api.*;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.model.TTTableModel;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.data.event.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RowEventSupport Integration Tests")
class RowEventSupportTest {

  private TTRegistry registry;
  private TTSchema<Person> schema;
  private TTTableModel<Person> model;
  private JTable table;
  private List<Person> testData;

  @BeforeEach
  void setUp() throws Exception {
    registry = TT.builder()
        .scanPackages("ru.dimension.tt.test.model")
        .build();

    schema = registry.schema(Person.class);
    model = new TTTableModel<>(schema);

    // Ensure we're on EDT for Swing operations
    SwingUtilities.invokeAndWait(() -> {
      table = new JTable(model);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    });

    testData = List.of(
        new Person(1L, "Alice", Person.Status.APPROVED, true),
        new Person(2L, "Bob", Person.Status.PENDING, false),
        new Person(3L, "Charlie", Person.Status.REJECTED, true)
    );

    model.setItems(testData);
  }

  @Nested
  @DisplayName("Mouse Click Events")
  class MouseClickEventTests {

    @Test
    @DisplayName("Single click triggers onClick handler")
    void singleClick() throws Exception {
      AtomicReference<RowEvent<Person>> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onClick(received::set)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Simulate single click on row 1
        MouseEvent click = createMouseEvent(MouseEvent.MOUSE_CLICKED, 1, 1, false);
        for (var listener : table.getMouseListeners()) {
          listener.mouseClicked(click);
        }
      });

      assertNotNull(received.get());
      assertEquals("Bob", received.get().row().getName());
      assertEquals(1, received.get().modelRowIndex());
    }

    @Test
    @DisplayName("Double click triggers onDoubleClick handler")
    void doubleClick() throws Exception {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onDoubleClickRow(received::set)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Simulate double click on row 0
        MouseEvent doubleClick = createMouseEvent(MouseEvent.MOUSE_CLICKED, 0, 2, false);
        for (var listener : table.getMouseListeners()) {
          listener.mouseClicked(doubleClick);
        }
      });

      assertNotNull(received.get());
      assertEquals("Alice", received.get().getName());
    }

    @Test
    @DisplayName("Double click does not trigger onClick handler")
    void doubleClickDoesNotTriggerSingleClick() throws Exception {
      AtomicInteger clickCount = new AtomicInteger(0);
      AtomicInteger doubleClickCount = new AtomicInteger(0);

      RowActions<Person> actions = RowActions.<Person>builder()
          .onClick(e -> clickCount.incrementAndGet())
          .onDoubleClick(e -> doubleClickCount.incrementAndGet())
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Simulate double click
        MouseEvent doubleClick = createMouseEvent(MouseEvent.MOUSE_CLICKED, 0, 2, false);
        for (var listener : table.getMouseListeners()) {
          listener.mouseClicked(doubleClick);
        }
      });

      assertEquals(0, clickCount.get());
      assertEquals(1, doubleClickCount.get());
    }

    @Test
    @DisplayName("Click outside rows does not trigger handler")
    void clickOutsideRows() throws Exception {
      AtomicBoolean triggered = new AtomicBoolean(false);

      RowActions<Person> actions = RowActions.<Person>builder()
          .onClick(e -> triggered.set(true))
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Simulate click at position where no row exists
        MouseEvent click = new MouseEvent(
            table, MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(), 0,
            10, 10000, // Y far below any row
            1, false, MouseEvent.BUTTON1
        );
        for (var listener : table.getMouseListeners()) {
          listener.mouseClicked(click);
        }
      });

      assertFalse(triggered.get());
    }

    private MouseEvent createMouseEvent(int id, int row, int clickCount, boolean isPopup) {
      Rectangle cellRect = table.getCellRect(row, 0, true);
      int x = cellRect.x + cellRect.width / 2;
      int y = cellRect.y + cellRect.height / 2;

      return new MouseEvent(
          table, id,
          System.currentTimeMillis(), 0,
          x, y, clickCount, isPopup, MouseEvent.BUTTON1
      );
    }
  }

  @Nested
  @DisplayName("Right Click and Context Menu")
  class RightClickTests {

    @Test
    @DisplayName("Right click triggers onRightClick handler")
    void rightClick() throws Exception {
      AtomicReference<RowEvent<Person>> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onRightClick(received::set)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        MouseEvent rightClick = createPopupTrigger(1);
        for (var listener : table.getMouseListeners()) {
          listener.mousePressed(rightClick);
        }
      });

      assertNotNull(received.get());
      assertEquals("Bob", received.get().row().getName());
    }

    @Test
    @DisplayName("Right click selects the row")
    void rightClickSelectsRow() throws Exception {
      RowActions<Person> actions = RowActions.<Person>builder()
          .onRightClick(e -> {})
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Initially no selection
        table.clearSelection();
        assertEquals(-1, table.getSelectedRow());

        MouseEvent rightClick = createPopupTrigger(2);
        for (var listener : table.getMouseListeners()) {
          listener.mousePressed(rightClick);
        }

        // Row should now be selected
        assertEquals(2, table.getSelectedRow());
      });
    }

    @Test
    @DisplayName("Context menu is shown on right click")
    void contextMenuShown() throws Exception {
      AtomicReference<JPopupMenu> shownMenu = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .contextMenu(event -> {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem("Edit " + event.row().getName()));
            return menu;
          })
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // We can't easily verify the popup was shown, but we can verify
        // the context menu provider returns the correct menu
        RowEvent<Person> event = RowEvent.of(testData.get(0), 0, 0, null);
        JPopupMenu menu = actions.contextMenuProvider().apply(event);

        assertNotNull(menu);
        assertEquals(1, menu.getComponentCount());
        assertTrue(((JMenuItem) menu.getComponent(0)).getText().contains("Alice"));
      });
    }

    @Test
    @DisplayName("Null context menu is handled gracefully")
    void nullContextMenu() throws Exception {
      AtomicBoolean noException = new AtomicBoolean(true);

      RowActions<Person> actions = RowActions.<Person>builder()
          .contextMenu(event -> null) // Return null
          .build();

      SwingUtilities.invokeAndWait(() -> {
        try {
          RowEventSupport.install(table, model, actions);

          MouseEvent rightClick = createPopupTrigger(0);
          for (var listener : table.getMouseListeners()) {
            listener.mousePressed(rightClick);
          }
        } catch (Exception e) {
          noException.set(false);
        }
      });

      assertTrue(noException.get());
    }

    @Test
    @DisplayName("Dynamic context menu based on row data")
    void dynamicContextMenu() throws Exception {
      RowActions<Person> actions = RowActions.<Person>builder()
          .contextMenuForRow(person -> {
            JPopupMenu menu = new JPopupMenu();
            if (person.isActive()) {
              menu.add(new JMenuItem("Deactivate"));
            } else {
              menu.add(new JMenuItem("Activate"));
            }
            return menu;
          })
          .build();

      // Test with active person (Alice)
      RowEvent<Person> activeEvent = RowEvent.of(testData.get(0), 0, 0, null);
      JPopupMenu activeMenu = actions.contextMenuProvider().apply(activeEvent);
      assertEquals("Deactivate", ((JMenuItem) activeMenu.getComponent(0)).getText());

      // Test with inactive person (Bob)
      RowEvent<Person> inactiveEvent = RowEvent.of(testData.get(1), 1, 1, null);
      JPopupMenu inactiveMenu = actions.contextMenuProvider().apply(inactiveEvent);
      assertEquals("Activate", ((JMenuItem) inactiveMenu.getComponent(0)).getText());
    }

    private MouseEvent createPopupTrigger(int row) {
      Rectangle cellRect = table.getCellRect(row, 0, true);
      int x = cellRect.x + cellRect.width / 2;
      int y = cellRect.y + cellRect.height / 2;

      // On most platforms, BUTTON3 with isPopupTrigger=true
      return new MouseEvent(
          table, MouseEvent.MOUSE_PRESSED,
          System.currentTimeMillis(), 0,
          x, y, 1, true, MouseEvent.BUTTON3
      );
    }
  }

  @Nested
  @DisplayName("Selection Change Events")
  class SelectionChangeTests {

    @Test
    @DisplayName("Selection change triggers handler")
    void selectionChange() throws Exception {
      List<RowSelectionEvent<Person>> events = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChanged(events::add)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Select row 0
        table.setRowSelectionInterval(0, 0);
      });

      // Wait for event processing
      Thread.sleep(50);

      assertEquals(1, events.size());
      assertEquals("Alice", events.get(0).selectedRow().getName());
      assertNull(events.get(0).previousRow());
    }

    @Test
    @DisplayName("Selection change from one row to another")
    void selectionChangeFromOneToAnother() throws Exception {
      List<RowSelectionEvent<Person>> events = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChanged(events::add)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Select row 0, then row 2
        table.setRowSelectionInterval(0, 0);
        table.setRowSelectionInterval(2, 2);
      });

      Thread.sleep(50);

      assertEquals(2, events.size());

      // First event: selected Alice, no previous
      assertEquals("Alice", events.get(0).selectedRow().getName());
      assertNull(events.get(0).previousRow());

      // Second event: selected Charlie, previous was Alice
      assertEquals("Charlie", events.get(1).selectedRow().getName());
      assertEquals("Alice", events.get(1).previousRow().getName());
    }

    @Test
    @DisplayName("Clear selection triggers handler")
    void clearSelection() throws Exception {
      List<RowSelectionEvent<Person>> events = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChanged(events::add)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        table.setRowSelectionInterval(1, 1);
        table.clearSelection();
      });

      Thread.sleep(50);

      assertEquals(2, events.size());

      RowSelectionEvent<Person> clearEvent = events.get(1);
      assertTrue(clearEvent.selectionCleared());
      assertNull(clearEvent.selectedRow());
      assertEquals("Bob", clearEvent.previousRow().getName());
    }

    @Test
    @DisplayName("Simplified selection handler receives selected row")
    void simplifiedSelectionHandler() throws Exception {
      List<Person> selectedPersons = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChangedRow(selectedPersons::add)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        table.setRowSelectionInterval(0, 0);
        table.setRowSelectionInterval(1, 1);
      });

      Thread.sleep(50);

      assertEquals(2, selectedPersons.size());
      assertEquals("Alice", selectedPersons.get(0).getName());
      assertEquals("Bob", selectedPersons.get(1).getName());
    }
  }

  @Nested
  @DisplayName("Hover Events")
  class HoverEventTests {

    @Test
    @DisplayName("Mouse enter row triggers hover handler")
    void mouseEnterRow() throws Exception {
      List<Object[]> hoverEvents = new ArrayList<>(); // [Person, Boolean]

      RowActions<Person> actions = RowActions.<Person>builder()
          .onHover((person, entered) -> hoverEvents.add(new Object[]{person, entered}))
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Simulate mouse move to row 1
        MouseEvent moveToRow1 = createMouseMoveEvent(1);
        for (var listener : table.getMouseMotionListeners()) {
          listener.mouseMoved(moveToRow1);
        }
      });

      assertEquals(1, hoverEvents.size());
      assertEquals("Bob", ((Person) hoverEvents.get(0)[0]).getName());
      assertTrue((Boolean) hoverEvents.get(0)[1]); // entered = true
    }

    @Test
    @DisplayName("Mouse exit row triggers hover handler with false")
    void mouseExitRow() throws Exception {
      List<Object[]> hoverEvents = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onHover((person, entered) -> hoverEvents.add(new Object[]{person, entered}))
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Move to row 0
        MouseEvent moveToRow0 = createMouseMoveEvent(0);
        for (var listener : table.getMouseMotionListeners()) {
          listener.mouseMoved(moveToRow0);
        }

        // Move to row 1 (should exit row 0)
        MouseEvent moveToRow1 = createMouseMoveEvent(1);
        for (var listener : table.getMouseMotionListeners()) {
          listener.mouseMoved(moveToRow1);
        }
      });

      assertEquals(3, hoverEvents.size());

      // Enter row 0
      assertEquals("Alice", ((Person) hoverEvents.get(0)[0]).getName());
      assertTrue((Boolean) hoverEvents.get(0)[1]);

      // Exit row 0
      assertEquals("Alice", ((Person) hoverEvents.get(1)[0]).getName());
      assertFalse((Boolean) hoverEvents.get(1)[1]);

      // Enter row 1
      assertEquals("Bob", ((Person) hoverEvents.get(2)[0]).getName());
      assertTrue((Boolean) hoverEvents.get(2)[1]);
    }

    @Test
    @DisplayName("Mouse exit table triggers hover exit")
    void mouseExitTable() throws Exception {
      List<Object[]> hoverEvents = new ArrayList<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onHover((person, entered) -> hoverEvents.add(new Object[]{person, entered}))
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Move to row 0
        MouseEvent moveToRow0 = createMouseMoveEvent(0);
        for (var listener : table.getMouseMotionListeners()) {
          listener.mouseMoved(moveToRow0);
        }

        // Exit table
        MouseEvent exitEvent = new MouseEvent(
            table, MouseEvent.MOUSE_EXITED,
            System.currentTimeMillis(), 0, -1, -1, 0, false
        );
        for (var listener : table.getMouseListeners()) {
          listener.mouseExited(exitEvent);
        }
      });

      assertEquals(2, hoverEvents.size());

      // Enter row 0
      assertTrue((Boolean) hoverEvents.get(0)[1]);

      // Exit row 0
      assertFalse((Boolean) hoverEvents.get(1)[1]);
    }

    private MouseEvent createMouseMoveEvent(int row) {
      Rectangle cellRect = table.getCellRect(row, 0, true);
      int x = cellRect.x + cellRect.width / 2;
      int y = cellRect.y + cellRect.height / 2;

      return new MouseEvent(
          table, MouseEvent.MOUSE_MOVED,
          System.currentTimeMillis(), 0, x, y, 0, false
      );
    }
  }

  @Nested
  @DisplayName("Enter Key Events")
  class EnterKeyTests {

    @Test
    @DisplayName("Enter key triggers handler for selected row")
    void enterKeyOnSelectedRow() throws Exception {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onEnterKey(received::set)
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        // Select row 1
        table.setRowSelectionInterval(1, 1);

        // Trigger Enter key action
        Action enterAction = table.getActionMap().get("tt.rowEnterAction");
        assertNotNull(enterAction);
        enterAction.actionPerformed(null);
      });

      assertNotNull(received.get());
      assertEquals("Bob", received.get().getName());
    }

    @Test
    @DisplayName("Enter key with no selection does not trigger handler")
    void enterKeyNoSelection() throws Exception {
      AtomicBoolean triggered = new AtomicBoolean(false);

      RowActions<Person> actions = RowActions.<Person>builder()
          .onEnterKey(p -> triggered.set(true))
          .build();

      SwingUtilities.invokeAndWait(() -> {
        RowEventSupport.install(table, model, actions);

        table.clearSelection();

        Action enterAction = table.getActionMap().get("tt.rowEnterAction");
        enterAction.actionPerformed(null);
      });

      assertFalse(triggered.get());
    }
  }

  @Nested
  @DisplayName("Empty/Null Actions Handling")
  class EmptyActionsTests {

    @Test
    @DisplayName("Null actions does not throw")
    void nullActions() throws Exception {
      AtomicBoolean noException = new AtomicBoolean(true);

      SwingUtilities.invokeAndWait(() -> {
        try {
          RowEventSupport.install(table, model, null);
        } catch (Exception e) {
          noException.set(false);
        }
      });

      assertTrue(noException.get());
    }

    @Test
    @DisplayName("Empty actions does not install any listeners")
    void emptyActions() throws Exception {
      int initialMouseListeners;
      int initialMotionListeners;

      SwingUtilities.invokeAndWait(() -> {
        RowActions<Person> actions = RowActions.<Person>builder().build();

        int beforeMouse = table.getMouseListeners().length;
        int beforeMotion = table.getMouseMotionListeners().length;

        RowEventSupport.install(table, model, actions);

        // No new listeners should be added
        assertEquals(beforeMouse, table.getMouseListeners().length);
        assertEquals(beforeMotion, table.getMouseMotionListeners().length);
      });
    }
  }
}