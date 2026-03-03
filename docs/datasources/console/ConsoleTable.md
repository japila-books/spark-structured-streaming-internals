# ConsoleTable

`ConsoleTable` is a `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table/)) with `SupportsWrite` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsWrite/)) support.

## Creating Instance

`ConsoleTable` takes no arguments to be created.

??? note "Scala object"
    `ConsoleTable` is an `object` in Scala which means it is a class that has exactly one instance (itself).
    A Scala `object` is created lazily when it is referenced for the first time.

    Learn more in [Tour of Scala](https://docs.scala-lang.org/tour/singleton-objects.html).

`ConsoleTable` is "created" when:

* `ConsoleSinkProvider` is requested for a [table](ConsoleSinkProvider.md#getTable)

## Name { #name }

??? note "Table"

    ```scala
    name(): String
    ```

    `name` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table/#name)) abstraction.

`name` is **console**.
