# Dimension-TT

Типизированная таблица Java для `JTable` и `JXTable`.

## Содержание

- [Почему Dimension-TT?](#почему-dimension-tt)
- [Ключевая философия](#ключевая-философия)
- [Требования](#требования)
- [Установка](#установка)
- [Использование](#использование)
    - [1. Определите типизированную строку](#1-определите-типизированную-строку)
    - [2. Соберите реестр со сканированием на этапе сборки](#2-соберите-реестр-со-сканированием-на-этапе-сборки)
    - [3. Создайте типизированную таблицу для JTable](#3-создайте-типизированную-таблицу-для-jtable)
    - [4. Создайте типизированную таблицу для JXTable](#4-создайте-типизированную-таблицу-для-jxtable)
    - [5. Иконки строк по enum-колонке](#5-иконки-строк-по-enum-колонке)
    - [6. Выбор (Selection)](#6-выбор-selection)
- [Документация](#документация)
- [Примечание](#примечание)
- [Контакты](#контакты)

## Почему Dimension-TT?

Таблицы Swing мощные, но типичный рабочий процесс по умолчанию — нетипизированный:

- вы вручную определяете колонки и их классы;
- вы постоянно пишете `addRow(new Object[]{...})`;
- вы дублируете логику порядка/имён колонок по всему коду;
- в итоге UI-код привязывается к индексам колонок.

Dimension-TT решает это, добавляя простой типизированный слой:

- **типизированные строки** (`T`) вместо `Object[]`;
- **схема из аннотаций** (`@TTColumn`) для предсказуемого порядка колонок и заголовков;
- **типизированные операции с данными**: `setItems(List<T>)`, `addItem(T)`, `selectedItem()`;
- **иконки строк**, определяемые данными строки/колонки (например, `DataType -> icon`);
- сканирование на этапе сборки с использованием **JDK Class-File API** для индексации схем без загрузки классов.

## Ключевая философия

Dimension-TT следует двухфазному подходу:

1) **Индексация на этапе сборки (сканирование classpath)**
- Сканирование пакетов и чтение аннотаций `@TTColumn` через **JDK Class-File API**
- Построение индекса в памяти (`TTScanIndex`), содержащего `TTColumnDef` (значения аннотаций)

2) **Привязка на этапе выполнения (быстрые аксессоры)**
- Когда вы запрашиваете схему для `Class<T>`, Dimension-TT:
    - использует заранее построенный индекс, чтобы создать `TTColumnSpec` (без reflection по аннотациям),
    - связывает геттеры/сеттеры через `MethodHandles` (без reflective invoke на горячем пути),
    - строит типизированную модель таблицы `TTTableModel<T>`.

Цель: **простой API, минимальные накладные расходы, предсказуемое поведение**.

## Требования

- Java 25+ с JDK Class-File API.

## Установка

Чтобы использовать Dimension-TT в Maven-проекте, добавьте зависимость в `pom.xml`:

```XML
<dependency>
  <groupId>ru.dimension</groupId>
  <artifactId>tt</artifactId>
  <version>${revision}</version>
</dependency>
```

> Примечания:
> - Если вы используете SwingX (`JXTable`), добавьте также модуль `tt-swingx` (имя artifact зависит от финальной схемы публикации).
> - Текущий репозиторий — это multi-module сборка (`tt-core`, `tt-swingx`).

## Использование

### 1. Определите типизированную строку

Аннотируйте поля или методы без аргументов с помощью `@TTColumn`.

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

### 2. Соберите реестр со сканированием на этапе сборки

`TTRegistry` хранит результаты сканирования и кэш схем.

```java
import ru.dimension.tt.api.TT;
import ru.dimension.tt.api.TTRegistry;

TTRegistry reg = TT.builder()
    .scanPackages("com.myapp.rows")
    .build();
```

### 3. Создайте типизированную таблицу для JTable

```java
import javax.swing.JTable;
import ru.dimension.tt.swing.JTableTables;
import ru.dimension.tt.swing.TTTable;

TTTable<ColumnRow, JTable> tt = JTableTables.create(reg, ColumnRow.class);

// типизированная вставка
public void handle() {
  tt.setItems(java.util.List.of(
      new ColumnRow(1, "CPU", DataType.NUMBER, "L1"),
      new ColumnRow(2, "Host", DataType.STRING, "L2")
  ));
}
```

### 4. Создайте типизированную таблицу для JXTable

```java
import org.jdesktop.swingx.JXTable;
import ru.dimension.tt.swing.TTTable;
import ru.dimension.tt.swingx.JXTableTables;

TTTable<ColumnRow, JXTable> tt = JXTableTables.create(reg, ColumnRow.class);
```

### 5. Иконки строк по enum-колонке

Иконки строк учитывают схему: иконка вычисляется из значения колонки по `columnId`.

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

// Поместить иконку в рендерер колонки "name"
var tt = JTableTables.create(
    reg,
    ColumnRow.class,
    TableUi.<ColumnRow>builder()
        .rowIcon(icons)
        .rowIconInColumn("name")
        .build()
);
```

### 6. Выбор (Selection)

```java
public handle() {
  tt.selectedItem().ifPresent(row -> {
    // row is ColumnRow
    System.out.println(row.getId() + " " + row.getName());
  });
}
```

## Документация

| EN                             | RU                                |
|:-------------------------------|:----------------------------------|
| [README in English](README.md) | [README на русском](README-RU.md) |

## Примечание

- Сканирование на этапе сборки сейчас индексирует методы по **имени** (ожидаются методы в стиле геттеров). Если нужна безопасная для перегрузок привязка, расширьте индекс так, чтобы он хранил дескрипторы методов.
- Если класса нет в индексе сканирования, Dimension-TT может откатиться к reflection-основанному загрузчику схем (настраивается).

## Контакты

Создано: [@akardapolov](mailto:akardapolov@yandex.ru)