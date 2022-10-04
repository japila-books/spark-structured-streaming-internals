# StatefulOperatorPartitioning

## <span id="getCompatibleDistribution"> getCompatibleDistribution

```scala
getCompatibleDistribution(
  expressions: Seq[Expression],
  numPartitions: Int,
  conf: SQLConf): Distribution
getCompatibleDistribution(
  expressions: Seq[Expression],
  stateInfo: StatefulOperatorStateInfo,
  conf: SQLConf): Distribution
```

`getCompatibleDistribution` returns the following `Distribution`s ([Spark SQL]({{ book.spark_sql }}/physical-operators/Distribution/)) based on [spark.sql.streaming.statefulOperator.useStrictDistribution](../configuration-properties.md#STATEFUL_OPERATOR_USE_STRICT_DISTRIBUTION) configuration property:

* `StatefulOpClusteredDistribution` when enabled
* `ClusteredDistribution` ([Spark SQL]({{ book.spark_sql }}/physical-operators/ClusteredDistribution)) otherwise

---

`getCompatibleDistribution` is used when:

* `BaseAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/BaseAggregateExec/)) is requested for the required child output distribution (of a streaming query)
* `UpdatingSessionsExec` is requested for the required child output distribution (of a streaming query)
* `FlatMapGroupsWithStateExec` is requested for the [required child output distribution](../physical-operators/FlatMapGroupsWithStateExec.md#requiredChildDistribution)
* `StateStoreRestoreExec` is requested for the [required child output distribution](../physical-operators/StateStoreRestoreExec.md#requiredChildDistribution)
* `StateStoreSaveExec` is requested for the [required child output distribution](../physical-operators/StateStoreSaveExec.md#requiredChildDistribution)
* `SessionWindowStateStoreRestoreExec` is requested for the [required child output distribution](../physical-operators/SessionWindowStateStoreRestoreExec.md#requiredChildDistribution)
* `SessionWindowStateStoreSaveExec` is requested for the [required child output distribution](../physical-operators/SessionWindowStateStoreSaveExec.md#requiredChildDistribution)
* `StreamingDeduplicateExec` is requested for the [required child output distribution](../physical-operators/StreamingDeduplicateExec.md#requiredChildDistribution)
