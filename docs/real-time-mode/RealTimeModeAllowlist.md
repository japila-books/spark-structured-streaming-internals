# RealTimeModeAllowlist

## Check Allowed Sinks { #checkAllowedSink }

```scala
checkAllowedSink(
  sink: Table,
  throwException: Boolean): Unit
```

`checkAllowedSink`...FIXME

---

`checkAllowedSink` is used when:

* `DataStreamWriter` is requested to [start a streaming query](../DataStreamWriter.md#startQuery) (with [RealTimeTrigger](RealTimeTrigger.md))

### Allowed Sinks { #allowedSinks }

```scala
allowedSinks: Set[String]
```

`allowedSinks` is a collection of the fully-qualified class names of the following `Table` sinks ([Spark SQL]({{ book.spark_sql }}/connector/Table/)):

* [ConsoleTable](../datasources/console/ConsoleTable.md)
* [ContinuousMemorySink](../datasources/memory/ContinuousMemorySink.md)
* [ForeachWriterTable](../datasources/foreach/ForeachWriterTable.md)
* [KafkaTable](../kafka/KafkaTable.md)
