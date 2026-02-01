package ru.dimension.tt.swing.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.dimension.tt.data.event.Person;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Row Event Data Classes Tests")
class RowEventTest {

  private final Person testPerson = new Person(1L, "Test", Person.Status.PENDING, true);

  @Nested
  @DisplayName("RowEvent")
  class RowEventTests {

    @Test
    @DisplayName("Factory method of() with MouseEvent")
    void ofWithMouseEvent() {
      MouseEvent mouseEvent = new MouseEvent(
          new JTable(), MouseEvent.MOUSE_CLICKED,
          System.currentTimeMillis(), 0, 10, 20, 1, false
      );

      RowEvent<Person> event = RowEvent.of(testPerson, 5, 3, mouseEvent);

      assertSame(testPerson, event.row());
      assertEquals(5, event.modelRowIndex());
      assertEquals(3, event.viewRowIndex());
      assertSame(mouseEvent, event.mouseEvent());
      assertTrue(event.hasMouseEvent());
    }

    @Test
    @DisplayName("Factory method of() without MouseEvent")
    void ofWithoutMouseEvent() {
      RowEvent<Person> event = RowEvent.of(testPerson, 5, 3);

      assertSame(testPerson, event.row());
      assertEquals(5, event.modelRowIndex());
      assertEquals(3, event.viewRowIndex());
      assertNull(event.mouseEvent());
      assertFalse(event.hasMouseEvent());
    }

    @Test
    @DisplayName("Record equality")
    void equality() {
      RowEvent<Person> event1 = RowEvent.of(testPerson, 5, 3);
      RowEvent<Person> event2 = RowEvent.of(testPerson, 5, 3);

      assertEquals(event1, event2);
      assertEquals(event1.hashCode(), event2.hashCode());
    }
  }

  @Nested
  @DisplayName("RowSelectionEvent")
  class RowSelectionEventTests {

    @Test
    @DisplayName("Selection with previous value")
    void selectionWithPrevious() {
      Person previous = new Person(0L, "Previous", Person.Status.REJECTED, false);

      RowSelectionEvent<Person> event = new RowSelectionEvent<>(
          testPerson, previous, 5, 3
      );

      assertTrue(event.hasSelection());
      assertFalse(event.selectionCleared());
      assertSame(testPerson, event.selectedRow());
      assertSame(previous, event.previousRow());
      assertEquals(5, event.selectedModelIndex());
      assertEquals(3, event.previousModelIndex());
    }

    @Test
    @DisplayName("Selection cleared")
    void selectionCleared() {
      RowSelectionEvent<Person> event = new RowSelectionEvent<>(
          null, testPerson, -1, 5
      );

      assertFalse(event.hasSelection());
      assertTrue(event.selectionCleared());
      assertNull(event.selectedRow());
      assertSame(testPerson, event.previousRow());
    }

    @Test
    @DisplayName("Initial selection (no previous)")
    void initialSelection() {
      RowSelectionEvent<Person> event = new RowSelectionEvent<>(
          testPerson, null, 0, -1
      );

      assertTrue(event.hasSelection());
      assertFalse(event.selectionCleared());
      assertNull(event.previousRow());
    }

    @Test
    @DisplayName("No selection, no previous")
    void noSelectionNoPrevious() {
      RowSelectionEvent<Person> event = new RowSelectionEvent<>(
          null, null, -1, -1
      );

      assertFalse(event.hasSelection());
      assertFalse(event.selectionCleared());
    }
  }
}