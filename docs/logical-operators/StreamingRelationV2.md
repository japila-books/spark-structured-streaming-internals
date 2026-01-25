---
title: StreamingRelationV2
---

# StreamingRelationV2 Leaf Logical Operator

`StreamingRelationV2` is a leaf logical operator that represents `SupportsRead` streaming tables (with `MICRO_BATCH_READ` or `CONTINUOUS_READ` capabilities) in a logical plan of a streaming query.

`StreamingRelationV2` is a `MultiInstanceRelation` ([Spark SQL]({{ book.spark_sql }}/logical-operators/MultiInstanceRelation)).

`StreamingRelationV2` is a `ExposesMetadataColumns` ([Spark SQL]({{ book.spark_sql }}/logical-operators/ExposesMetadataColumns)).

!!! note "Leaf Logical Operators"
    Learn more about [Leaf Logical Operators]({{ book.spark_sql }}/logical-operators/LeafNode), [SupportsRead]({{ book.spark_sql }}/connector/SupportsRead) and [Table Capabilities]({{ book.spark_sql }}/connector/TableCapability) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

## Creating Instance

`StreamingRelationV2` takes the following to be created:

* <span id="source"> Source `TableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/TableProvider))
* <span id="sourceName"> Source Name
* <span id="table"> `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table))
* <span id="extraOptions"> Extra Options
* <span id="output"> Output Attributes ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="catalog"> `CatalogPlugin` ([Spark SQL]({{ book.spark_sql }}/connector/catalog/CatalogPlugin))
* <span id="identifier"> `Identifier`
* <span id="v1Relation"> V1 Relation `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))

`StreamingRelationV2` is created when:

* `RelationResolution` ([Spark SQL]({{ book.spark_sql }}/RelationResolution)) is requested to resolve a relation
* `StreamingRelationV2` is requested to [withMetadataColumns](#withMetadataColumns)
* `ResolveDataSource` ([Spark SQL]({{ book.spark_sql }}/ResolveDataSource)) logical analysis rule is executed (for a `SupportsRead` table with `MICRO_BATCH_READ` or `CONTINUOUS_READ` capabilities)
* `MemoryStreamBase` is requested for a [logical query plan](../datasources/memory/MemoryStreamBase.md#logicalPlan)

## Logical Resolution

`StreamingRelationV2` is resolved to the following leaf logical operators:

* [StreamingDataSourceV2Relation](StreamingDataSourceV2Relation.md) or [StreamingExecutionRelation](StreamingExecutionRelation.md) when `MicroBatchExecution` stream execution engine is requested for an [analyzed logical plan](../micro-batch-execution/MicroBatchExecution.md#logicalPlan)
* [StreamingDataSourceV2Relation](StreamingDataSourceV2Relation.md) when `ContinuousExecution` stream execution engine is created (and initializes an [analyzed logical plan](../continuous-execution/ContinuousExecution.md#logicalPlan))

## Metadata Columns { #metadataOutput }

??? note "LogicalPlan"

    ```scala
    metadataOutput: Seq[AttributeReference]
    ```

    `metadataOutput` is part of the `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan/#metadataOutput)) abstraction.

`metadataOutput` checks out whether this [Table](#table) is `SupportsMetadataColumns` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsMetadataColumns/)) for the extra metadata columns.

Otherwise, `metadataOutput` returns no metadata columns (`Nil`).

??? note "Lazy Value"
    `metadataOutput` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

## Add Metadata Columns to Output Columns { #withMetadataColumns }

??? note "ExposesMetadataColumns"

    ```scala
    withMetadataColumns(): StreamingRelationV2
    ```

    `withMetadataColumns` is part of the `ExposesMetadataColumns` ([Spark SQL]({{ book.spark_sql }}/logical-operators/ExposesMetadataColumns/#withMetadataColumns)) abstraction.

`withMetadataColumns` determines whether thare are any extra [metadata columns](#metadataOutput) to be added to this [output](#output).

If so, `withMetadataColumns` creates a new `StreamingRelationV2` with the extra [metadata columns](#metadataOutput) added. Otherwise, `withMetadataColumns` does nothing.

## isStreaming { #isStreaming }

??? note "LogicalPlan"

    ```scala
    isStreaming: Boolean
    ```

    `isStreaming` is part of the `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan/#isStreaming)) abstraction.

`isStreaming` is always enabled (`true`).
