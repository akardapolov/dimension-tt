package ru.dimension.tt.api;

public final class TT {
  private TT() {}

  public static TTBuilder builder() {
    return new TTBuilder();
  }
}