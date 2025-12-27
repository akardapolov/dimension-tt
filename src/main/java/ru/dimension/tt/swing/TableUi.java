package ru.dimension.tt.swing;

import ru.dimension.tt.swing.icon.RowIconPlacement;
import ru.dimension.tt.swing.icon.RowIconProvider;

public final class TableUi<T> {

  private final RowIconProvider<T> rowIconProvider;
  private final RowIconPlacement rowIconPlacement;

  private TableUi(Builder<T> b) {
    this.rowIconProvider = b.rowIconProvider;
    this.rowIconPlacement = b.rowIconPlacement;
  }

  public RowIconProvider<T> rowIconProvider() {
    return rowIconProvider;
  }

  public RowIconPlacement rowIconPlacement() {
    return rowIconPlacement;
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {
    private RowIconProvider<T> rowIconProvider;
    private RowIconPlacement rowIconPlacement;

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

    public TableUi<T> build() {
      return new TableUi<>(this);
    }
  }
}