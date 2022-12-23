# WriteToStream Logical Operator

`WriteToStream` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)) that represents [WriteToStreamStatement](WriteToStreamStatement.md) operator at execution time (of a streaming query).

`WriteToStream` is used to create the stream execution engines:

* [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md#plan)
* [ContinuousExecution](../continuous-execution/ContinuousExecution.md#plan)

## Creating Instance

`WriteToStream` takes the following to be created:

* <span id="name"> Name
* <span id="resolvedCheckpointLocation"> Checkpoint Location
* <span id="sink"> Sink `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table/))
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="deleteCheckpointOnStop"> `deleteCheckpointOnStop` flag
* <span id="inputQuery"> `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan)) of the Input Query
* <span id="catalogAndIdent"> Optional `TableCatalog` and `Identifier` (default: undefined)

`WriteToStream` is created when:

* [ResolveWriteToStream](../logical-analysis-rules/ResolveWriteToStream.md) logical resolution rule is executed (to resolve [WriteToStreamStatement](WriteToStreamStatement.md) operators)

## <span id="isStreaming"> isStreaming

```scala
isStreaming: Boolean
```

`isStreaming` is part of the `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#isStreaming)) abstraction.

---

`isStreaming` is `true`.

## <span id="output"> Output Schema

```scala
output: Seq[Attribute]
```

`output` is part of the `QueryPlan` ([Spark SQL]({{ book.spark_sql }}/catalyst/QueryPlan#output)) abstraction.

---

`output` is empty (`Nil`)
