package ru.dimension.tt.schema;

@FunctionalInterface
public interface TTSchemaLoader {
  TTSchema<?> loadSchema(Class<?> rowType);

  @SuppressWarnings("unchecked")
  default <T> TTSchema<T> load(Class<T> rowType) {
    return (TTSchema<T>) loadSchema((Class<?>) rowType);
  }
}