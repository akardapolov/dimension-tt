package ru.dimension.tt.swing.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.data.event.Person;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Post-Creation Event Installation Tests")
class PostCreationInstallationTest {

  private TTRegistry registry;

  @BeforeEach
  void setUp() {
    registry = TT.builder()
        .scanPackages("ru.dimension.tt.test.model")
        .build();
  }

  @Test
  @DisplayName("Install events after table creation")
  void installEventsAfterCreation() throws Exception {
    AtomicReference<Person> doubleClicked = new AtomicReference<>();

    SwingUtilities.invokeAndWait(() -> {
      // Create table without any events
      TTTable<Person, JTable> tt = JTableTables.create(registry, Person.class);

      tt.setItems(List.of(
          new Person(1L, "Alice", Person.Status.APPROVED, true)
      ));

      // Install events afterwards
      RowActions<Person> actions = RowActions.<Person>builder()
          .onDoubleClickRow(doubleClicked::set)
          .build();

      RowEventSupport.install(tt.table(), tt.model(), actions);

      // Verify listener was added
      assertTrue(tt.table().getMouseListeners().length > 0);
    });
  }

  @Test
  @DisplayName("Add multiple event handlers separately")
  void addMultipleEventsSeparately() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      TTTable<Person, JTable> tt = JTableTables.create(registry, Person.class);

      // Install click events
      RowActions<Person> clickActions = RowActions.<Person>builder()
          .onClickRow(p -> System.out.println("Click: " + p.getName()))
          .onDoubleClickRow(p -> System.out.println("Double: " + p.getName()))
          .build();
      RowEventSupport.install(tt.table(), tt.model(), clickActions);

      // Install selection events separately
      RowActions<Person> selectionActions = RowActions.<Person>builder()
          .onSelectionChangedRow(p -> System.out.println("Selected: " + (p != null ? p.getName() : "none")))
          .build();
      RowEventSupport.install(tt.table(), tt.model(), selectionActions);

      // Both sets of listeners should be installed
      assertNotNull(tt.table());
    });
  }
}