== [[StreamingAggregationStateManagerImplV1]] StreamingAggregationStateManagerImplV1 -- Legacy State Manager for Streaming Aggregation

`StreamingAggregationStateManagerImplV1` is the legacy <<spark-sql-streaming-StreamingAggregationStateManagerBaseImpl.adoc#, state manager for streaming aggregations>>.

NOTE: The version of a state manager is controlled using <<spark-sql-streaming-properties.adoc#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>> internal configuration property.

`StreamingAggregationStateManagerImplV1` is <<creating-instance, created>> exclusively when `StreamingAggregationStateManager` is requested for a <<spark-sql-streaming-StreamingAggregationStateManager.adoc#createStateManager, new StreamingAggregationStateManager>>.

=== [[put]] Storing Row in State Store -- `put` Method

[source, scala]
----
put(store: StateStore, row: UnsafeRow): Unit
----

NOTE: `put` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#put, StreamingAggregationStateManager Contract>> to store a row in a state store.

`put`...FIXME

=== [[creating-instance]] Creating StreamingAggregationStateManagerImplV1 Instance

`StreamingAggregationStateManagerImplV1` takes the following when created:

* [[keyExpressions]] Attribute expressions for keys (`Seq[Attribute]`)
* [[inputRowAttributes]] Attribute expressions of input rows (`Seq[Attribute]`)
