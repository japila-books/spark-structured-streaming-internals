# RealTimeModeAllowlist

## Allowed Sinks { #allowedSinks }

`allowedSinks` is a collection of the following class names of the `Table` sinks ([Spark SQL]({{ book.spark_sql }}/connector/Table/)):

* `org.apache.spark.sql.execution.streaming.ConsoleTable`
* `org.apache.spark.sql.execution.streaming.sources.ContinuousMemorySink`
* [ForeachWriterTable](../datasources/foreach/ForeachWriterTable.md)
* [KafkaTable](../kafka/KafkaTable.md)
