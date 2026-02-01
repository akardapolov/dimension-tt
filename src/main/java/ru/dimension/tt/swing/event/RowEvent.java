package ru.dimension.tt.swing.event;

import java.awt.event.MouseEvent;

/**
 * Represents a row event with all context information.
 */
public record RowEvent<T>(
    T row,
    int modelRowIndex,
    int viewRowIndex,
    MouseEvent mouseEvent  // null for non-mouse events (e.g., keyboard navigation)
) {
  public boolean hasMouseEvent() {
    return mouseEvent != null;
  }

  public static <T> RowEvent<T> of(T row, int modelRow, int viewRow, MouseEvent e) {
    return new RowEvent<>(row, modelRow, viewRow, e);
  }

  public static <T> RowEvent<T> of(T row, int modelRow, int viewRow) {
    return new RowEvent<>(row, modelRow, viewRow, null);
  }
}