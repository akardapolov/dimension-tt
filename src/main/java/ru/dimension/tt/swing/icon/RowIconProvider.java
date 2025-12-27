package ru.dimension.tt.swing.icon;

import javax.swing.Icon;

@FunctionalInterface
public interface RowIconProvider<T> {

  RowIcon getIcon(T row);

  record RowIcon(Icon icon, String tooltip) {
    public static RowIcon of(Icon icon) { return new RowIcon(icon, null); }
    public static RowIcon of(Icon icon, String tooltip) { return new RowIcon(icon, tooltip); }
  }
}