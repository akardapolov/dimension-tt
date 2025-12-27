package ru.dimension.tt.swing.icon;

import javax.swing.Icon;

@FunctionalInterface
public interface IconMapper<V> {
  Icon iconFor(V value);

  default String tooltipFor(V value) {
    return null;
  }
}