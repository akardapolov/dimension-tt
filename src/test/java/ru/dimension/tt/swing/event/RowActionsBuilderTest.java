package ru.dimension.tt.swing.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.data.event.Person;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RowActions Builder Tests")
class RowActionsBuilderTest {

  private Person testPerson;

  @BeforeEach
  void setUp() {
    testPerson = new Person(1L, "John Doe", Person.Status.APPROVED, true);
  }

  @Nested
  @DisplayName("Builder configuration")
  class BuilderConfigurationTests {

    @Test
    @DisplayName("Empty builder creates empty RowActions")
    void emptyBuilder() {
      RowActions<Person> actions = RowActions.<Person>builder().build();

      assertTrue(actions.isEmpty());
      assertNull(actions.onClick());
      assertNull(actions.onDoubleClick());
      assertNull(actions.onRightClick());
      assertNull(actions.contextMenuProvider());
      assertNull(actions.onHover());
      assertNull(actions.onSelectionChanged());
      assertNull(actions.onRowEnterKey());
    }

    @Test
    @DisplayName("Builder with single action is not empty")
    void singleAction() {
      RowActions<Person> actions = RowActions.<Person>builder()
          .onClickRow(p -> {})
          .build();

      assertFalse(actions.isEmpty());
      assertNotNull(actions.onClick());
    }

    @Test
    @DisplayName("Builder with all actions configured")
    void allActionsConfigured() {
      RowActions<Person> actions = RowActions.<Person>builder()
          .onClick(e -> {})
          .onDoubleClick(e -> {})
          .onRightClick(e -> {})
          .contextMenu(e -> new JPopupMenu())
          .onHover((p, entered) -> {})
          .onSelectionChanged(e -> {})
          .onEnterKey(p -> {})
          .build();

      assertFalse(actions.isEmpty());
      assertNotNull(actions.onClick());
      assertNotNull(actions.onDoubleClick());
      assertNotNull(actions.onRightClick());
      assertNotNull(actions.contextMenuProvider());
      assertNotNull(actions.onHover());
      assertNotNull(actions.onSelectionChanged());
      assertNotNull(actions.onRowEnterKey());
    }
  }

  @Nested
  @DisplayName("Simplified row handlers")
  class SimplifiedRowHandlerTests {

    @Test
    @DisplayName("onClickRow receives correct row object")
    void onClickRow() {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onClickRow(received::set)
          .build();

      RowEvent<Person> event = RowEvent.of(testPerson, 0, 0, null);
      actions.onClick().accept(event);

      assertSame(testPerson, received.get());
    }

    @Test
    @DisplayName("onDoubleClickRow receives correct row object")
    void onDoubleClickRow() {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onDoubleClickRow(received::set)
          .build();

      RowEvent<Person> event = RowEvent.of(testPerson, 5, 3, null);
      actions.onDoubleClick().accept(event);

      assertSame(testPerson, received.get());
    }

    @Test
    @DisplayName("contextMenuForRow receives correct row object")
    void contextMenuForRow() {
      AtomicReference<Person> received = new AtomicReference<>();
      JPopupMenu expectedMenu = new JPopupMenu();

      RowActions<Person> actions = RowActions.<Person>builder()
          .contextMenuForRow(p -> {
            received.set(p);
            return expectedMenu;
          })
          .build();

      RowEvent<Person> event = RowEvent.of(testPerson, 0, 0, null);
      JPopupMenu resultMenu = actions.contextMenuProvider().apply(event);

      assertSame(testPerson, received.get());
      assertSame(expectedMenu, resultMenu);
    }

    @Test
    @DisplayName("onSelectionChangedRow receives correct row object")
    void onSelectionChangedRow() {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChangedRow(received::set)
          .build();

      RowSelectionEvent<Person> event = new RowSelectionEvent<>(testPerson, null, 0, -1);
      actions.onSelectionChanged().accept(event);

      assertSame(testPerson, received.get());
    }

    @Test
    @DisplayName("onSelectionChangedRow handles null selection")
    void onSelectionChangedRowNull() {
      AtomicReference<Person> received = new AtomicReference<>(testPerson);

      RowActions<Person> actions = RowActions.<Person>builder()
          .onSelectionChangedRow(received::set)
          .build();

      RowSelectionEvent<Person> event = new RowSelectionEvent<>(null, testPerson, -1, 0);
      actions.onSelectionChanged().accept(event);

      assertNull(received.get());
    }
  }

  @Nested
  @DisplayName("Full event handlers")
  class FullEventHandlerTests {

    @Test
    @DisplayName("onClick receives full RowEvent")
    void onClickFullEvent() {
      AtomicReference<RowEvent<Person>> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onClick(received::set)
          .build();

      RowEvent<Person> event = RowEvent.of(testPerson, 10, 8, null);
      actions.onClick().accept(event);

      assertSame(event, received.get());
      assertEquals(10, received.get().modelRowIndex());
      assertEquals(8, received.get().viewRowIndex());
    }

    @Test
    @DisplayName("onDoubleClick receives full RowEvent with MouseEvent")
    void onDoubleClickWithMouseEvent() {
      AtomicReference<RowEvent<Person>> received = new AtomicReference<>();
      MouseEvent mockMouseEvent = new MouseEvent(
          new JTable(), MouseEvent.MOUSE_CLICKED,
          System.currentTimeMillis(), 0, 100, 50, 2, false
      );

      RowActions<Person> actions = RowActions.<Person>builder()
          .onDoubleClick(received::set)
          .build();

      RowEvent<Person> event = RowEvent.of(testPerson, 5, 5, mockMouseEvent);
      actions.onDoubleClick().accept(event);

      assertTrue(received.get().hasMouseEvent());
      assertEquals(100, received.get().mouseEvent().getX());
      assertEquals(50, received.get().mouseEvent().getY());
    }

    @Test
    @DisplayName("onHover receives row and entered flag")
    void onHover() {
      AtomicReference<Person> receivedPerson = new AtomicReference<>();
      AtomicBoolean receivedEntered = new AtomicBoolean();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onHover((p, entered) -> {
            receivedPerson.set(p);
            receivedEntered.set(entered);
          })
          .build();

      // Test enter
      actions.onHover().accept(testPerson, true);
      assertSame(testPerson, receivedPerson.get());
      assertTrue(receivedEntered.get());

      // Test exit
      actions.onHover().accept(testPerson, false);
      assertFalse(receivedEntered.get());
    }

    @Test
    @DisplayName("onEnterKey receives row object")
    void onEnterKey() {
      AtomicReference<Person> received = new AtomicReference<>();

      RowActions<Person> actions = RowActions.<Person>builder()
          .onEnterKey(received::set)
          .build();

      actions.onRowEnterKey().accept(testPerson);

      assertSame(testPerson, received.get());
    }
  }
}