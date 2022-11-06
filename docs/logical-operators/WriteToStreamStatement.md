# WriteToStreamStatement Logical Operator

`WriteToStreamStatement` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)).

## Creating Instance

`WriteToStreamStatement` takes the following to be created:

* <span id="userSpecifiedName"> User-specified Name
* <span id="userSpecifiedCheckpointLocation"> Checkpoint Location
* <span id="useTempCheckpointLocation"> `useTempCheckpointLocation` flag
* <span id="recoverFromCheckpointLocation"> `recoverFromCheckpointLocation` flag
* <span id="sink"> Sink `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table/))
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="hadoopConf"> Hadoop `Configuration`
* <span id="isContinuousTrigger"> `isContinuousTrigger` flag
* <span id="inputQuery"> `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan)) of the Input Query
* <span id="catalogAndIdent"> Optional `TableCatalog` and `Identifier` (default: undefined)

`WriteToStreamStatement` is created when:

* `StreamingQueryManager` is requested to [create a streaming query](../StreamingQueryManager.md#createQuery)

## Logical Resolution

`WriteToStreamStatement` is resolved to [WriteToStream](WriteToStream.md) operator using [ResolveWriteToStream](../logical-analysis-rules/ResolveWriteToStream.md) logical resolution rule.
