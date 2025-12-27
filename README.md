# Dimension-TT

Java Typed Table for JTable and JXTable.

## Contents

- [Why Dimension-TT?](#why-dimension-tt)
- [Core Philosophy](#core-philosophy)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
  - [1. Define a typed row](#1-define-a-typed-row)
  - [2. Build a registry with build-time scan](#2-build-a-registry-with-build-time-scan)
  - [3. Create a JTable typed table](#3-create-a-jtable-typed-table)
  - [4. Create a JXTable typed table](#4-create-a-jxtable-typed-table)
  - [5. Row icons by enum column](#5-row-icons-by-enum-column)
  - [6. Selection](#6-selection)
- [Documentation](#documentation)
- [Notice](#notice)
- [Contact](#contact)

## Why Dimension-TT?

Swing tables are powerful, but the default workflow is usually untyped:

- you manually define columns and their classes;
- you keep writing `addRow(new Object[]{...})`;
- you duplicate column order/name logic across the codebase;
- you end up coupling UI code to column indices.

Dimension-TT solves this by introducing a simple, typed layer:

- **typed rows** (`T`) instead of `Object[]`;
- **schema from annotations** (`@TTColumn`) for predictable column order and titles;
- **typed data operations**: `setItems(List<T>)`, `addItem(T)`, `selectedItem()`;
- **row icons** driven by row/column data (e.g., `DataType -> icon`);
- build-time scan using **JDK Class-File API** to index schemas without loading classes.

## Core Philosophy

Dimension-TT follows a two-phase approach:

1) **Build-time indexing (classpath scan)**
- Scan packages and read `@TTColumn` annotations via the **JDK Class-File API**
- Build an in-memory index (`TTScanIndex`) containing `TTColumnDef` (annotation values)

2) **Runtime binding (fast accessors)**
- When you request a schema for `Class<T>`, Dimension-TT:
  - uses the prebuilt index to create `TTColumnSpec` (no annotation reflection needed),
  - binds getters/setters with `MethodHandles` (no reflective invoke in the hot path),
  - builds a typed table model `TTTableModel<T>`.

The goal is: **simple API, minimal overhead, predictable behavior**.

## Requirements

- Java 25+ with JDK Class-File API.

## Installation

To use Dimension-TT in your Maven project, add the following dependency to your `pom.xml`:

```XML
<dependency>
  <groupId>ru.dimension</groupId>
  <artifactId>tt</artifactId>
  <version>${revision}</version>
</dependency>
```

> Notes:
> - If you use SwingX (`JXTable`), add the `tt-swingx` module as well (artifact name depends on your final publishing layout).
> - Current repository is a multi-module build (`tt-core`, `tt-swingx`).

## Usage

### 1. Define a typed row

Annotate fields or 0-arg methods with `@TTColumn`.

```java
import ru.dimension.tt.annotation.ColumnKind;
import ru.dimension.tt.annotation.TTColumn;

public class ColumnRow {

  @TTColumn(id = "id", order = 0, name = "ID", kind = ColumnKind.NUMBER, visible = false)
  private int id;

  @TTColumn(id = "name", order = 1, name = "Name", minWidth = 80, preferredWidth = 180)
  private String name;

  @TTColumn(id = "type", order = 2, name = "Type", kind = ColumnKind.TEXT)
  private DataType type;

  private String label;

  @TTColumn(id = "label", order = 3, name = "Label", editable = true, setter = "setLabel", maxWidth = 300)
  public String getLabel() { return label; }

  public void setLabel(String label) { this.label = label; }

  public ColumnRow(int id, String name, DataType type, String label) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.label = label;
  }

  public int getId() { return id; }
  public String getName() { return name; }
  public DataType getType() { return type; }
}
```

### 2. Build a registry with build-time scan

`TTRegistry` stores scan results and schema cache.

```java
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;

TTRegistry reg = TT.builder()
    .scanPackages("com.myapp.rows")
    .build();
```

### 3. Create a JTable typed table

```java
import javax.swing.JTable;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;

TTTable<ColumnRow, JTable> tt = JTableTables.create(reg, ColumnRow.class);

// typed insert
public void handle() {
  tt.setItems(java.util.List.of(
      new ColumnRow(1, "CPU", DataType.NUMBER, "L1"),
      new ColumnRow(2, "Host", DataType.STRING, "L2")
  ));
}
```

### 4. Create a JXTable typed table

```java
import org.jdesktop.swingx.JXTable;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swingx.JXTableTables;

TTTable<ColumnRow, JXTable> tt = JXTableTables.create(reg, ColumnRow.class);
```

### 5. Row icons by enum column

Row icons are schema-aware: the icon is derived from a column value by `columnId`.

```java
import javax.swing.Icon;
import ru.dimension.tt.swing.TableUi;
import ru.dimension.tt.swing.icon.*;

Icon icon123 = new Icon() {};
Icon iconAbc = new Icon() {};

IconMapper<DataType> mapper = new IconMapper<>() {
  @Override
  public Icon iconFor(DataType value) {
    return switch (value) {
      case NUMBER -> icon123;
      case STRING -> iconAbc;
      default -> null;
    };
  }

  @Override
  public String tooltipFor(DataType value) {
    return "Type: " + value;
  }
};

var schema = reg.schema(ColumnRow.class);

RowIconProvider<ColumnRow> icons =
    RowIconProviders.byEnumColumn(schema, "type", DataType.class, mapper);

// Put the icon into the "name" column renderer
var tt = JTableTables.create(
    reg,
    ColumnRow.class,
    TableUi.<ColumnRow>builder()
        .rowIcon(icons)
        .rowIconInColumn("name")
        .build()
);
```

### 6. Selection

```java
public handle() {
  tt.selectedItem().ifPresent(row -> {
    // row is ColumnRow
    System.out.println(row.getId() + " " + row.getName());
  });
}
```

## Documentation

| EN                             | RU                                |
|:-------------------------------|:----------------------------------|
| [README in English](README.md) | [README на русском](README-RU.md) |

## Notice

- Build-time scan indexes methods currently by **name** (getter-style methods are expected). If you need overload-safe matching, extend the index to store method descriptors.
- If a class is not present in the scan index, Dimension-TT can fall back to a reflection-based schema loader (configurable).

## Contact

Created by [@akardapolov](mailto:akardapolov@yandex.ru)