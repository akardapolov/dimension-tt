package ru.dimension.tt.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TTColumn {
  String id() default "";
  int order();
  String name();

  ColumnKind kind() default ColumnKind.AUTO;

  boolean visible() default true;
  boolean editable() default false;

  int minWidth() default -1;
  int maxWidth() default -1;
  int preferredWidth() default -1;

  String setter() default "";
}