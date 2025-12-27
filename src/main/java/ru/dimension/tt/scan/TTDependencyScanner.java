package ru.dimension.tt.scan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.AttributedElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.ConstantValueEntry;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import ru.dimension.tt.annotation.ColumnKind;

import static ru.dimension.tt.scan.TTScanIndex.MemberKind;

public final class TTDependencyScanner {

  private static final String TT_COLUMN_DESC = "Lru/dimension/tt/annotation/TTColumn;";

  private TTDependencyScanner() {}

  public static TTScanIndex scan(ClassLoader classLoader, String... basePackages) {
    try {
      Set<String> classNames = discoverClassNames(classLoader, basePackages);
      Map<String, TTScanIndex.TTRowTypeDef> defs = analyzeClasses(classLoader, classNames);
      return new TTScanIndex(defs);
    } catch (Exception e) {
      throw new RuntimeException("Dimension-TT: Failed to scan packages: " + Arrays.toString(basePackages), e);
    }
  }

  private static Map<String, TTScanIndex.TTRowTypeDef> analyzeClasses(ClassLoader cl, Set<String> classNames)
      throws IOException {

    Map<String, TTScanIndex.TTRowTypeDef> out = new HashMap<>();

    for (String className : classNames) {
      byte[] classBytes = readClassBytes(cl, className);
      ClassModel cm = ClassFile.of().parse(classBytes);

      List<TTScanIndex.TTMemberDef> members = new ArrayList<>();

      for (FieldModel f : cm.fields()) {
        Annotation ann = findAnnotation(f, TT_COLUMN_DESC);
        if (ann != null) {
          TTScanIndex.TTColumnDef def = parseTTColumnDef(ann, f.fieldName().stringValue());
          members.add(new TTScanIndex.TTMemberDef(MemberKind.FIELD, f.fieldName().stringValue(), def));
        }
      }

      for (MethodModel m : cm.methods()) {
        String mn = m.methodName().stringValue();
        if ("<init>".equals(mn)) continue;

        Annotation ann = findAnnotation(m, TT_COLUMN_DESC);
        if (ann != null) {
          TTScanIndex.TTColumnDef def = parseTTColumnDef(ann, mn);
          members.add(new TTScanIndex.TTMemberDef(MemberKind.METHOD, mn, def));
        }
      }

      if (!members.isEmpty()) {
        out.put(className, new TTScanIndex.TTRowTypeDef(className, List.copyOf(members)));
      }
    }

    return out;
  }

  private static TTScanIndex.TTColumnDef parseTTColumnDef(Annotation ann, String fallbackId) {
    // Defaults from @TTColumn
    String id = "";
    Integer order = null;     // required (no default)
    String name = null;       // required (no default)
    ColumnKind kind = ColumnKind.AUTO;
    boolean visible = true;
    boolean editable = false;
    int minWidth = -1;
    int maxWidth = -1;
    int preferredWidth = -1;
    String setter = "";

    for (var evp : ann.elements()) {
      String elementName = evp.name().stringValue();
      Object value = toJavaValue(evp.value());

      switch (elementName) {
        case "id" -> id = asString(value, "");
        case "order" -> order = asInt(value, null);
        case "name" -> name = asString(value, null);
        case "kind" -> kind = asEnum(value, ColumnKind.class, ColumnKind.AUTO);
        case "visible" -> visible = asBoolean(value, true);
        case "editable" -> editable = asBoolean(value, false);
        case "minWidth" -> minWidth = asInt(value, -1);
        case "maxWidth" -> maxWidth = asInt(value, -1);
        case "preferredWidth" -> preferredWidth = asInt(value, -1);
        case "setter" -> setter = asString(value, "");
        default -> {
          // ignore unknown future fields
        }
      }
    }

    if (order == null) {
      throw new IllegalArgumentException("@TTColumn missing required element 'order' on: " + fallbackId);
    }
    if (name == null) {
      throw new IllegalArgumentException("@TTColumn missing required element 'name' on: " + fallbackId);
    }

    String resolvedId = (id == null || id.isBlank()) ? fallbackId : id;

    return new TTScanIndex.TTColumnDef(
        resolvedId,
        order,
        name,
        kind,
        visible,
        editable,
        minWidth,
        maxWidth,
        preferredWidth,
        setter
    );
  }

  /**
   * Convert Class-File API AnnotationValue to a plain Java value:
   * - constants -> boxed primitives/String
   * - enum -> enum constant name (String)
   */
  private static Object toJavaValue(AnnotationValue v) {
    return switch (v) {
      case AnnotationValue.OfConstant c -> c.constant().constantValue();
      case AnnotationValue.OfEnum e -> e.constantName().stringValue();
      default -> null;
    };
  }

  private static Object constantToJava(ConstantValueEntry c) {
    // constantValue() typically returns boxed primitives or String
    return c.constantValue();
  }

  private static String asString(Object v, String def) {
    if (v == null) return def;
    if (v instanceof String s) return s;
    return String.valueOf(v);
  }

  private static Integer asInt(Object v, Integer def) {
    if (v == null) return def;
    if (v instanceof Integer i) return i;
    if (v instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(v.toString());
    } catch (Exception e) {
      return def;
    }
  }

  private static boolean asBoolean(Object v, boolean def) {
    if (v == null) return def;
    if (v instanceof Boolean b) return b;
    if (v instanceof Integer i) return i != 0; // annotation booleans are often encoded as 0/1 ints
    if (v instanceof Number n) return n.intValue() != 0;
    return Boolean.parseBoolean(v.toString());
  }

  private static <E extends Enum<E>> E asEnum(Object v, Class<E> enumType, E def) {
    if (v == null) return def;
    if (enumType.isInstance(v)) return enumType.cast(v);
    // for AnnotationValue.OfEnum we store constant name as String
    try {
      return Enum.valueOf(enumType, v.toString());
    } catch (Exception e) {
      return def;
    }
  }

  private static Annotation findAnnotation(AttributedElement element, String descriptor) {
    for (var attr : element.attributes()) {
      List<Annotation> annotations = null;

      if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
        annotations = rva.annotations();
      } else if (attr instanceof RuntimeInvisibleAnnotationsAttribute ria) {
        annotations = ria.annotations();
      }

      if (annotations != null) {
        for (Annotation a : annotations) {
          if (a.className().stringValue().equals(descriptor)) {
            return a;
          }
        }
      }
    }
    return null;
  }

  private static byte[] readClassBytes(ClassLoader cl, String className) throws IOException {
    String resourceName = className.replace('.', '/') + ".class";
    try (InputStream is = cl.getResourceAsStream(resourceName)) {
      if (is == null) throw new IOException("Resource not found: " + resourceName);
      return is.readAllBytes();
    }
  }

  private static Set<String> discoverClassNames(ClassLoader cl, String... basePackages) throws IOException {
    Set<String> classNames = new HashSet<>();

    for (String basePackage : basePackages) {
      String path = basePackage.replace('.', '/');
      Enumeration<URL> resources = cl.getResources(path);

      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();

        if ("file".equals(resource.getProtocol())) {
          try {
            classNames.addAll(findClassesInDirectory(basePackage, Paths.get(resource.toURI())));
          } catch (java.net.URISyntaxException e) {
            throw new IOException(e);
          }
        } else if ("jar".equals(resource.getProtocol())) {
          classNames.addAll(findClassesInJar(basePackage, resource));
        }
      }
    }

    return classNames;
  }

  private static Set<String> findClassesInDirectory(String basePackage, Path directory) throws IOException {
    Set<String> classes = new HashSet<>();
    if (!Files.isDirectory(directory)) return classes;

    String packagePathPart = basePackage.replace('.', File.separatorChar);

    try (var stream = Files.walk(directory)) {
      stream.filter(p -> p.toString().endsWith(".class"))
          .forEach(path -> {
            String fullPath = path.toString();
            int packageStartIndex = fullPath.indexOf(packagePathPart);
            if (packageStartIndex != -1) {
              String className = fullPath
                  .substring(packageStartIndex)
                  .replace(File.separatorChar, '.')
                  .replace(".class", "");
              classes.add(className);
            }
          });
    }

    return classes;
  }

  private static Set<String> findClassesInJar(String basePackage, URL jarUrl) throws IOException {
    Set<String> classes = new HashSet<>();
    JarURLConnection conn = (JarURLConnection) jarUrl.openConnection();
    String pathPrefix = basePackage.replace('.', '/') + "/";

    try (JarFile jar = conn.getJarFile()) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith(pathPrefix) && name.endsWith(".class") && !entry.isDirectory()) {
          classes.add(name.replace('/', '.').replace(".class", ""));
        }
      }
    }

    return classes;
  }
}