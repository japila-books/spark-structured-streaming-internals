# ResolveWriteToStream Logical Resolution Rule

`ResolveWriteToStream` is a logical resolution rule (`Rule[LogicalPlan]`) to resolve [WriteToStreamStatement](../logical-operators/WriteToStreamStatement.md) logical operators (into [WriteToStream](../logical-operators/WriteToStream.md)s).

`ResolveWriteToStream` is part of `extendedResolutionRules` ([Spark SQL]({{ book.spark_sql }}/Analyzer#extendedResolutionRules)) of an `Analyzer` of the following:

* `HiveSessionStateBuilder` ([Spark SQL]({{ book.spark_sql }}/hive/HiveSessionStateBuilder#analyzer))
* `BaseSessionStateBuilder` ([Spark SQL]({{ book.spark_sql }}/BaseSessionStateBuilder#analyzer))

## Creating Instance

`ResolveWriteToStream` takes no arguments to be created.
