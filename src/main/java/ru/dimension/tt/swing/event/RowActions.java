package ru.dimension.tt.swing.event;

import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.JPopupMenu;

/**
 * Configuration holder for all row-level actions.
 */
public final class RowActions<T> {

  private final Consumer<RowEvent<T>> onClick;
  private final Consumer<RowEvent<T>> onDoubleClick;
  private final Consumer<RowEvent<T>> onRightClick;
  private final Function<RowEvent<T>, JPopupMenu> contextMenuProvider;
  private final BiConsumer<T, Boolean> onHover;  // row, isEntered
  private final Consumer<RowSelectionEvent<T>> onSelectionChanged;
  private final Consumer<T> onRowEnterKey;  // Enter key pressed on selected row

  private RowActions(Builder<T> b) {
    this.onClick = b.onClick;
    this.onDoubleClick = b.onDoubleClick;
    this.onRightClick = b.onRightClick;
    this.contextMenuProvider = b.contextMenuProvider;
    this.onHover = b.onHover;
    this.onSelectionChanged = b.onSelectionChanged;
    this.onRowEnterKey = b.onRowEnterKey;
  }

  public Consumer<RowEvent<T>> onClick() { return onClick; }
  public Consumer<RowEvent<T>> onDoubleClick() { return onDoubleClick; }
  public Consumer<RowEvent<T>> onRightClick() { return onRightClick; }
  public Function<RowEvent<T>, JPopupMenu> contextMenuProvider() { return contextMenuProvider; }
  public BiConsumer<T, Boolean> onHover() { return onHover; }
  public Consumer<RowSelectionEvent<T>> onSelectionChanged() { return onSelectionChanged; }
  public Consumer<T> onRowEnterKey() { return onRowEnterKey; }

  public boolean isEmpty() {
    return onClick == null && onDoubleClick == null && onRightClick == null
        && contextMenuProvider == null && onHover == null
        && onSelectionChanged == null && onRowEnterKey == null;
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {
    private Consumer<RowEvent<T>> onClick;
    private Consumer<RowEvent<T>> onDoubleClick;
    private Consumer<RowEvent<T>> onRightClick;
    private Function<RowEvent<T>, JPopupMenu> contextMenuProvider;
    private BiConsumer<T, Boolean> onHover;
    private Consumer<RowSelectionEvent<T>> onSelectionChanged;
    private Consumer<T> onRowEnterKey;

    /**
     * Single click on row.
     */
    public Builder<T> onClick(Consumer<RowEvent<T>> handler) {
      this.onClick = handler;
      return this;
    }

    /**
     * Single click - simplified version accepting just the row object.
     */
    public Builder<T> onClickRow(Consumer<T> handler) {
      this.onClick = e -> handler.accept(e.row());
      return this;
    }

    /**
     * Double click on row.
     */
    public Builder<T> onDoubleClick(Consumer<RowEvent<T>> handler) {
      this.onDoubleClick = handler;
      return this;
    }

    /**
     * Double click - simplified version.
     */
    public Builder<T> onDoubleClickRow(Consumer<T> handler) {
      this.onDoubleClick = e -> handler.accept(e.row());
      return this;
    }

    /**
     * Right click on row (before context menu).
     */
    public Builder<T> onRightClick(Consumer<RowEvent<T>> handler) {
      this.onRightClick = handler;
      return this;
    }

    /**
     * Context menu provider - return null to show no menu.
     */
    public Builder<T> contextMenu(Function<RowEvent<T>, JPopupMenu> provider) {
      this.contextMenuProvider = provider;
      return this;
    }

    /**
     * Context menu - simplified version using just the row.
     */
    public Builder<T> contextMenuForRow(Function<T, JPopupMenu> provider) {
      this.contextMenuProvider = e -> provider.apply(e.row());
      return this;
    }

    /**
     * Mouse hover over row (enter/exit).
     * BiConsumer receives (row, isEntered).
     */
    public Builder<T> onHover(BiConsumer<T, Boolean> handler) {
      this.onHover = handler;
      return this;
    }

    /**
     * Row selection changed (keyboard navigation, mouse click, programmatic).
     */
    public Builder<T> onSelectionChanged(Consumer<RowSelectionEvent<T>> handler) {
      this.onSelectionChanged = handler;
      return this;
    }

    /**
     * Selection changed - simplified version receiving just the newly selected row (or null).
     */
    public Builder<T> onSelectionChangedRow(Consumer<T> handler) {
      this.onSelectionChanged = e -> handler.accept(e.selectedRow());
      return this;
    }

    /**
     * Enter key pressed on selected row (common UX: open/edit item).
     */
    public Builder<T> onEnterKey(Consumer<T> handler) {
      this.onRowEnterKey = handler;
      return this;
    }

    public RowActions<T> build() {
      return new RowActions<>(this);
    }
  }
}