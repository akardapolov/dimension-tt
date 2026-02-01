package ru.dimension.tt.swing.event;

/**
 * Represents selection change event.
 */
public record RowSelectionEvent<T>(
    T selectedRow,      // null if nothing selected
    T previousRow,      // null if nothing was selected before
    int selectedModelIndex,   // -1 if nothing selected
    int previousModelIndex    // -1 if nothing was selected before
) {
  public boolean hasSelection() {
    return selectedRow != null;
  }

  public boolean selectionCleared() {
    return previousRow != null && selectedRow == null;
  }
}