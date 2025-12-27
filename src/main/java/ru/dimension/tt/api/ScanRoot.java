package ru.dimension.tt.api;

import java.nio.file.Path;

public sealed interface ScanRoot permits ScanRoot.Dir, ScanRoot.Jar {
  record Dir(Path path) implements ScanRoot {}
  record Jar(Path path) implements ScanRoot {}

  static ScanRoot dir(Path p) { return new Dir(p); }
  static ScanRoot jar(Path p) { return new Jar(p); }
}