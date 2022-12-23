# ResolveWriteToStream Logical Resolution Rule

`ResolveWriteToStream` is a logical resolution rule (`Rule[LogicalPlan]`) to resolve [WriteToStreamStatement](../logical-operators/WriteToStreamStatement.md) unary logical operators (into [WriteToStream](../logical-operators/WriteToStream.md)s).

`ResolveWriteToStream` is part of `extendedResolutionRules` ([Spark SQL]({{ book.spark_sql }}/Analyzer#extendedResolutionRules)) of an `Analyzer` of the following:

* `HiveSessionStateBuilder` ([Spark SQL]({{ book.spark_sql }}/hive/HiveSessionStateBuilder#analyzer))
* `BaseSessionStateBuilder` ([Spark SQL]({{ book.spark_sql }}/BaseSessionStateBuilder#analyzer))

## Creating Instance

`ResolveWriteToStream` takes no arguments to be created.

## <span id="apply"> Executing Rule

```scala
apply(
  plan: LogicalPlan): LogicalPlan
```

`apply` is part of the `Rule` ([Spark SQL]({{ book.spark_sql }}/catalyst/Rule#apply)) abstraction.

---

`apply` resolves [WriteToStreamStatement](../logical-operators/WriteToStreamStatement.md) unary logical operators into [WriteToStream](../logical-operators/WriteToStream.md)s.

`apply` [resolveCheckpointLocation](#resolveCheckpointLocation).

With [spark.sql.adaptive.enabled](../configuration-properties.md#spark.sql.adaptive.enabled) enabled, `apply`...FIXME

With [spark.sql.streaming.unsupportedOperationCheck](../configuration-properties.md#spark.sql.streaming.unsupportedOperationCheck) enabled, `apply`...FIXME
