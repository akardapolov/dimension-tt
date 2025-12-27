package ru.dimension.tt.schema;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.annotation.TTColumn;

public final class ReflectionSchemaLoader implements TTSchemaLoader {

  @Override
  public TTSchema<?> loadSchema(Class<?> rowType) {
    return loadTyped(cast(rowType));
  }

  private static <T> Class<T> cast(Class<?> c) {
    @SuppressWarnings("unchecked")
    Class<T> t = (Class<T>) c;
    return t;
  }

  private <T> TTSchema<T> loadTyped(Class<T> rowType) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandles.Lookup priv = privateLookup(rowType, lookup);

    List<TTColumnBinding<T>> cols = new ArrayList<>();

    for (Field f : rowType.getDeclaredFields()) {
      TTColumn ann = f.getAnnotation(TTColumn.class);
      if (ann == null) continue;

      String id = !ann.id().isBlank() ? ann.id() : f.getName();
      ColumnKind kind = inferKind(ann.kind(), f.getType());

      TTColumnSpec spec = new TTColumnSpec(
          id,
          ann.order(),
          ann.name(),
          kind,
          ann.visible(),
          ann.editable(),
          ann.minWidth(),
          ann.maxWidth(),
          ann.preferredWidth()
      );

      MethodHandle getterHandle;
      try {
        getterHandle = priv.findGetter(rowType, f.getName(), f.getType());
      } catch (IllegalAccessException | NoSuchFieldException e) {
        throw new IllegalStateException("Cannot access field getter: " + rowType.getName() + "#" + f.getName(), e);
      }

      Function<T, Object> getter = (obj) -> {
        try {
          return getterHandle.invoke(obj);
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      };

      BiConsumer<T, Object> setter = null;
      if (ann.editable()) {
        try {
          MethodHandle setterHandle = priv.findSetter(rowType, f.getName(), f.getType());
          setter = (obj, v) -> {
            try {
              setterHandle.invoke(obj, v);
            } catch (Throwable t) {
              throw new RuntimeException(t);
            }
          };
        } catch (Throwable ignored) {
        }
      }

      cols.add(new TTColumnBinding<>(spec, inferColumnClass(kind, f.getType()), getter, setter));
    }

    for (Method m : rowType.getDeclaredMethods()) {
      TTColumn ann = m.getAnnotation(TTColumn.class);
      if (ann == null) continue;

      if (m.getParameterCount() != 0) {
        throw new IllegalArgumentException("@TTColumn method must have 0 params: " + rowType.getName() + "#" + m.getName());
      }

      String id = !ann.id().isBlank() ? ann.id() : m.getName();
      ColumnKind kind = inferKind(ann.kind(), m.getReturnType());

      TTColumnSpec spec = new TTColumnSpec(
          id,
          ann.order(),
          ann.name(),
          kind,
          ann.visible(),
          ann.editable(),
          ann.minWidth(),
          ann.maxWidth(),
          ann.preferredWidth()
      );

      MethodHandle getterHandle;
      try {
        getterHandle = priv.unreflect(m);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Cannot access method getter: " + rowType.getName() + "#" + m.getName(), e);
      }

      Function<T, Object> getter = (obj) -> {
        try {
          return getterHandle.invoke(obj);
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      };

      BiConsumer<T, Object> setter = null;
      if (ann.editable()) {
        Method setterMethod = resolveSetter(rowType, m, ann.setter());
        if (setterMethod != null) {
          try {
            MethodHandle setterHandle = priv.unreflect(setterMethod);
            setter = (obj, v) -> {
              try {
                setterHandle.invoke(obj, v);
              } catch (Throwable t) {
                throw new RuntimeException(t);
              }
            };
          } catch (IllegalAccessException ignored) {
          }
        }
      }

      cols.add(new TTColumnBinding<>(spec, inferColumnClass(kind, m.getReturnType()), getter, setter));
    }

    if (cols.isEmpty()) {
      throw new IllegalArgumentException("No @TTColumn found on type: " + rowType.getName());
    }

    cols.sort(Comparator.comparingInt(c -> c.spec().order()));
    return new TTSchema<>(rowType, cols);
  }

  private static MethodHandles.Lookup privateLookup(Class<?> type, MethodHandles.Lookup parent) {
    try {
      return MethodHandles.privateLookupIn(type, parent);
    } catch (IllegalAccessException e) {
      return parent;
    }
  }

  private static ColumnKind inferKind(ColumnKind declared, Class<?> javaType) {
    if (declared != ColumnKind.AUTO) return declared;

    if (javaType == boolean.class || javaType == Boolean.class) return ColumnKind.CHECKBOX;
    if (Number.class.isAssignableFrom(wrap(javaType))) return ColumnKind.NUMBER;
    if (java.util.Date.class.isAssignableFrom(javaType) || javaType.getName().startsWith("java.time.")) return ColumnKind.DATE;

    return ColumnKind.TEXT;
  }

  private static Class<?> inferColumnClass(ColumnKind kind, Class<?> javaType) {
    return switch (kind) {
      case CHECKBOX -> Boolean.class;
      case NUMBER -> wrap(javaType);
      case DATE -> javaType;
      case TEXT, AUTO -> String.class;
    };
  }

  private static Class<?> wrap(Class<?> t) {
    if (!t.isPrimitive()) return t;
    if (t == int.class) return Integer.class;
    if (t == long.class) return Long.class;
    if (t == double.class) return Double.class;
    if (t == float.class) return Float.class;
    if (t == short.class) return Short.class;
    if (t == byte.class) return Byte.class;
    if (t == boolean.class) return Boolean.class;
    if (t == char.class) return Character.class;
    return t;
  }

  private static Method resolveSetter(Class<?> type, Method getter, String explicitSetter) {
    if (explicitSetter != null && !explicitSetter.isBlank()) {
      for (Method m : type.getDeclaredMethods()) {
        if (m.getName().equals(explicitSetter) && m.getParameterCount() == 1) return m;
      }
      return null;
    }

    String g = getter.getName();
    String base;
    if (g.startsWith("get") && g.length() > 3) base = g.substring(3);
    else if (g.startsWith("is") && g.length() > 2) base = g.substring(2);
    else return null;

    String setterName = "set" + base;
    for (Method m : type.getDeclaredMethods()) {
      if (m.getName().equals(setterName) && m.getParameterCount() == 1) return m;
    }
    return null;
  }
}