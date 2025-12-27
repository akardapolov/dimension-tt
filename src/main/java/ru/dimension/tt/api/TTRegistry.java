package ru.dimension.tt.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ru.dimension.tt.scan.TTScanIndex;
import ru.dimension.tt.schema.TTSchema;
import ru.dimension.tt.schema.TTSchemaLoader;

public final class TTRegistry {

  private final ClassLoader classLoader;
  private final TTSchemaLoader schemaLoader;
  private final List<ScanRoot> scanRoots;
  private final TTScanIndex scanIndex;

  private final Map<Class<?>, TTSchema<?>> cache = new ConcurrentHashMap<>();

  public TTRegistry(ClassLoader classLoader,
                    TTSchemaLoader schemaLoader,
                    List<ScanRoot> scanRoots,
                    TTScanIndex scanIndex) {
    this.classLoader = classLoader;
    this.schemaLoader = schemaLoader;
    this.scanRoots = List.copyOf(scanRoots);
    this.scanIndex = scanIndex;
  }

  public ClassLoader classLoader() { return classLoader; }
  public List<ScanRoot> scanRoots() { return scanRoots; }
  public TTScanIndex scanIndex() { return scanIndex; }

  public Set<String> indexedRowTypes() {
    return scanIndex.rows().keySet();
  }

  @SuppressWarnings("unchecked")
  public <T> TTSchema<T> schema(Class<T> rowType) {
    return (TTSchema<T>) cache.computeIfAbsent(rowType, schemaLoader::loadSchema);
  }
}