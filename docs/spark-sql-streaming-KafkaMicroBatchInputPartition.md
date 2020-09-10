== [[KafkaMicroBatchInputPartition]] KafkaMicroBatchInputPartition

`KafkaMicroBatchInputPartition` is an `InputPartition` (of `InternalRows`) that is used (<<creating-instance, created>>) exclusively when `KafkaMicroBatchReader` is requested for <<spark-sql-streaming-KafkaMicroBatchReader.md#planInputPartitions, input partitions>> (when `DataSourceV2ScanExec` physical operator is requested for the partitions of the input RDD).

[[creating-instance]]
`KafkaMicroBatchInputPartition` takes the following to be created:

* [[offsetRange]] <<spark-sql-streaming-KafkaOffsetRangeCalculator.md#KafkaOffsetRange, KafkaOffsetRange>>
* [[executorKafkaParams]] Kafka parameters used for Kafka clients on executors (`Map[String, Object]`)
* [[pollTimeoutMs]] Poll timeout (in ms)
* [[failOnDataLoss]] `failOnDataLoss` flag
* [[reuseKafkaConsumer]] `reuseKafkaConsumer` flag

[[createPartitionReader]]
`KafkaMicroBatchInputPartition` creates a <<spark-sql-streaming-KafkaMicroBatchInputPartitionReader.md#, KafkaMicroBatchInputPartitionReader>> when requested for a `InputPartitionReader[InternalRow]` (as a part of the `InputPartition` contract).

[[preferredLocations]]
`KafkaMicroBatchInputPartition` simply requests the given <<offsetRange, KafkaOffsetRange>> for the optional `preferredLoc` when requested for `preferredLocations` (as a part of the `InputPartition` contract).
