package ru.dimension.tt.swing;

import ru.dimension.tt.swing.event.RowActions;
import ru.dimension.tt.swing.icon.RowIconPlacement;
import ru.dimension.tt.swing.icon.RowIconProvider;

public final class TableUi<T> {

  private final RowIconProvider<T> rowIconProvider;
  private final RowIconPlacement rowIconPlacement;
  private final RowActions<T> rowActions;

  private TableUi(Builder<T> b) {
    this.rowIconProvider = b.rowIconProvider;
    this.rowIconPlacement = b.rowIconPlacement;
    this.rowActions = b.rowActions;
  }

  public RowIconProvider<T> rowIconProvider() { return rowIconProvider; }
  public RowIconPlacement rowIconPlacement() { return rowIconPlacement; }
  public RowActions<T> rowActions() { return rowActions; }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {
    private RowIconProvider<T> rowIconProvider;
    private RowIconPlacement rowIconPlacement;
    private RowActions<T> rowActions;

    public Builder<T> rowIcon(RowIconProvider<T> p) {
      this.rowIconProvider = p;
      return this;
    }

    public Builder<T> rowIconPlacement(RowIconPlacement placement) {
      this.rowIconPlacement = placement;
      return this;
    }

    public Builder<T> rowIconInColumn(String columnId) {
      this.rowIconPlacement = RowIconPlacement.inColumn(columnId);
      return this;
    }

    public Builder<T> rowActions(RowActions<T> actions) {
      this.rowActions = actions;
      return this;
    }

    // Convenience methods for common actions
    public Builder<T> onDoubleClick(java.util.function.Consumer<T> handler) {
      ensureRowActionsBuilder().onDoubleClickRow(handler);
      return this;
    }

    public Builder<T> onSelectionChanged(java.util.function.Consumer<T> handler) {
      ensureRowActionsBuilder().onSelectionChangedRow(handler);
      return this;
    }

    public Builder<T> onEnterKey(java.util.function.Consumer<T> handler) {
      ensureRowActionsBuilder().onEnterKey(handler);
      return this;
    }

    public Builder<T> contextMenu(java.util.function.Function<T, javax.swing.JPopupMenu> provider) {
      ensureRowActionsBuilder().contextMenuForRow(provider);
      return this;
    }

    private RowActions.Builder<T> rowActionsBuilder;

    private RowActions.Builder<T> ensureRowActionsBuilder() {
      if (rowActionsBuilder == null) {
        rowActionsBuilder = RowActions.builder();
      }
      return rowActionsBuilder;
    }

    public TableUi<T> build() {
      if (rowActionsBuilder != null && rowActions == null) {
        rowActions = rowActionsBuilder.build();
      }
      return new TableUi<>(this);
    }
  }
}