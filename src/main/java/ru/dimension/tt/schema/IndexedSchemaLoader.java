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
import ru.dimension.tt.scan.TTScanIndex;

public final class IndexedSchemaLoader implements TTSchemaLoader {

  private final TTScanIndex index;
  private final TTSchemaLoader fallback;

  public IndexedSchemaLoader(TTScanIndex index, TTSchemaLoader fallback) {
    this.index = index;
    this.fallback = fallback;
  }

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
    TTScanIndex.TTRowTypeDef def = index.rows().get(rowType.getName());
    if (def == null) {
      return fallback.load(rowType);
    }

    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandles.Lookup priv = privateLookup(rowType, lookup);

    List<TTColumnBinding<T>> cols = new ArrayList<>();

    for (TTScanIndex.TTMemberDef md : def.members()) {
      switch (md.kind()) {
        case FIELD -> bindField(rowType, priv, cols, md);
        case METHOD -> bindMethod(rowType, priv, cols, md);
      }
    }

    if (cols.isEmpty()) {
      // Если индекс есть, но ничего не смогли забиндить (класс изменился после скана)
      return fallback.load(rowType);
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

  private static <T> void bindField(Class<T> rowType,
                                    MethodHandles.Lookup priv,
                                    List<TTColumnBinding<T>> out,
                                    TTScanIndex.TTMemberDef md) {
    try {
      Field f = rowType.getDeclaredField(md.name());
      TTScanIndex.TTColumnDef cd = md.column();

      ColumnKind kind = inferKind(cd.kind(), f.getType());

      TTColumnSpec spec = new TTColumnSpec(
          cd.id(),
          cd.order(),
          cd.name(),
          kind,
          cd.visible(),
          cd.editable(),
          cd.minWidth(),
          cd.maxWidth(),
          cd.preferredWidth()
      );

      MethodHandle getterHandle = priv.findGetter(rowType, f.getName(), f.getType());
      Function<T, Object> getter = (obj) -> {
        try {
          return getterHandle.invoke(obj);
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      };

      BiConsumer<T, Object> setter = null;
      if (cd.editable()) {
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
          // editable=true, но сеттер недоступен/поле final -> runtime read-only
        }
      }

      out.add(new TTColumnBinding<>(spec, inferColumnClass(kind, f.getType()), getter, setter));
    } catch (NoSuchFieldException ignored) {
      // member из индекса не найден в классе (класс поменялся)
    } catch (Throwable t) {
      throw new RuntimeException("Failed to bind field column: " + rowType.getName() + "#" + md.name(), t);
    }
  }

  private static <T> void bindMethod(Class<T> rowType,
                                     MethodHandles.Lookup priv,
                                     List<TTColumnBinding<T>> out,
                                     TTScanIndex.TTMemberDef md) {
    TTScanIndex.TTColumnDef cd = md.column();

    try {
      Method getterMethod = findZeroArgMethod(rowType, md.name());
      if (getterMethod == null) {
        return;
      }

      ColumnKind kind = inferKind(cd.kind(), getterMethod.getReturnType());

      TTColumnSpec spec = new TTColumnSpec(
          cd.id(),
          cd.order(),
          cd.name(),
          kind,
          cd.visible(),
          cd.editable(),
          cd.minWidth(),
          cd.maxWidth(),
          cd.preferredWidth()
      );

      MethodHandle getterHandle = priv.unreflect(getterMethod);
      Function<T, Object> getter = (obj) -> {
        try {
          return getterHandle.invoke(obj);
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      };

      BiConsumer<T, Object> setter = null;
      if (cd.editable()) {
        Method setterMethod = resolveSetter(rowType, getterMethod, cd.setter());
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

      out.add(new TTColumnBinding<>(spec, inferColumnClass(kind, getterMethod.getReturnType()), getter, setter));
    } catch (Throwable t) {
      throw new RuntimeException("Failed to bind method column: " + rowType.getName() + "#" + md.name(), t);
    }
  }

  private static Method findZeroArgMethod(Class<?> type, String name) {
    for (Method m : type.getDeclaredMethods()) {
      if (m.getName().equals(name) && m.getParameterCount() == 0) {
        return m;
      }
    }
    return null;
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