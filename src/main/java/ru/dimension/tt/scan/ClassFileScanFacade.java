package ru.dimension.tt.scan;

import java.util.List;
import ru.dimension.tt.api.ScanRoot;

/**
 * Каркас под build-time scan classpath без загрузки классов.
 * В следующей итерации сюда добавится чтение @TTColumn через JDK Class-File API
 * и построение “schema defs”.
 */
public final class ClassFileScanFacade {
  private ClassFileScanFacade() {}

  public static void scan(List<ScanRoot> roots) {
    // TODO: реализовать через java.lang.classfile (JDK 25+)
  }
}