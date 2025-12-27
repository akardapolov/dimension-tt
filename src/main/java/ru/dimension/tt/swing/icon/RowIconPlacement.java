package ru.dimension.tt.swing.icon;

public sealed interface RowIconPlacement permits RowIconPlacement.InColumn {
  record InColumn(String columnId) implements RowIconPlacement {}

  static RowIconPlacement inColumn(String columnId) {
    return new InColumn(columnId);
  }
}