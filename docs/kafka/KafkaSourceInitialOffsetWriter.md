# KafkaSourceInitialOffsetWriter

`KafkaSourceInitialOffsetWriter` is a [HDFSMetadataLog](../HDFSMetadataLog.md) of [KafkaSourceOffset](KafkaSourceOffset.md)s.

## Creating Instance

`KafkaSourceInitialOffsetWriter` takes the following to be created:

* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="metadataPath"> Path of the metadata log directory

`KafkaSourceInitialOffsetWriter` is created when:

* `KafkaMicroBatchStream` is requested to [getOrCreateInitialPartitionOffsets](KafkaMicroBatchStream.md#getOrCreateInitialPartitionOffsets)
* `KafkaSource` is requested to [getOrCreateInitialPartitionOffsets](KafkaSource.md#getOrCreateInitialPartitionOffsets)

## <span id="deserialize"> deserialize

```scala
deserialize(
  in: InputStream): KafkaSourceOffset
```

`deserialize` is part of the [HDFSMetadataLog](../HDFSMetadataLog.md#deserialize) abstraction.

---

`deserialize` creates a [KafkaSourceOffset](KafkaSourceOffset.md) (from a `SerializedOffset`).
