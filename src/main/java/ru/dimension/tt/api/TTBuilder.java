package ru.dimension.tt.api;

import java.util.ArrayList;
import java.util.List;
import ru.dimension.tt.scan.TTDependencyScanner;
import ru.dimension.tt.scan.TTScanIndex;
import ru.dimension.tt.schema.IndexedSchemaLoader;
import ru.dimension.tt.schema.ReflectionSchemaLoader;
import ru.dimension.tt.schema.TTSchemaLoader;

public final class TTBuilder {

  private final List<ScanRoot> scanRoots = new ArrayList<>();
  private final List<String> basePackages = new ArrayList<>();

  private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

  // fallback для типов вне индекса (можно позже заменить на on-demand classfile loader)
  private TTSchemaLoader fallbackSchemaLoader = new ReflectionSchemaLoader();

  public TTBuilder classLoader(ClassLoader cl) {
    this.classLoader = cl;
    return this;
  }

  public TTBuilder addScanRoot(ScanRoot root) {
    this.scanRoots.add(root);
    return this;
  }

  public TTBuilder scanPackages(String... pkgs) {
    this.basePackages.addAll(List.of(pkgs));
    return this;
  }

  public TTBuilder fallbackSchemaLoader(TTSchemaLoader loader) {
    this.fallbackSchemaLoader = loader;
    return this;
  }

  public TTRegistry build() {
    TTScanIndex index = basePackages.isEmpty()
        ? new TTScanIndex(java.util.Map.of())
        : TTDependencyScanner.scan(classLoader, basePackages.toArray(String[]::new));

    TTSchemaLoader loader = new IndexedSchemaLoader(index, fallbackSchemaLoader);
    return new TTRegistry(classLoader, loader, scanRoots, index);
  }
}