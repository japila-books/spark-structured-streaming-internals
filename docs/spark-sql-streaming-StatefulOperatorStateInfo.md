== [[StatefulOperatorStateInfo]] StatefulOperatorStateInfo

[[creating-instance]]
`StatefulOperatorStateInfo` identifies the state store for a given stateful physical operator:

* [[checkpointLocation]] Checkpoint directory (`checkpointLocation`)
* [[queryRunId]] <<StreamingQuery.md#runId, Run ID>> of a streaming query (`queryRunId`)
* [[operatorId]] Stateful operator ID (`operatorId`)
* [[storeVersion]] <<state-version, State version>> (`storeVersion`)
* [[numPartitions]] Number of partitions

`StatefulOperatorStateInfo` is <<creating-instance, created>> exclusively when `IncrementalExecution` is requested for <<spark-sql-streaming-IncrementalExecution.md#nextStatefulOperationStateInfo, nextStatefulOperationStateInfo>>.

[[toString]]
When requested for a textual representation (`toString`), `StatefulOperatorStateInfo` returns the following:

```
state info [ checkpoint = [checkpointLocation], runId = [queryRunId], opId = [operatorId], ver = [storeVersion], numPartitions = [numPartitions]]
```

=== [[state-version]] State Version and Batch ID

When <<creating-instance, created>> (when `IncrementalExecution` is requested for the <<spark-sql-streaming-IncrementalExecution.md#nextStatefulOperationStateInfo, next StatefulOperatorStateInfo>>), a `StatefulOperatorStateInfo` is given a <<storeVersion, state version>>.

The <<storeVersion, state version>> is exactly the <<spark-sql-streaming-IncrementalExecution.md#currentBatchId, batch ID>> of the <<spark-sql-streaming-IncrementalExecution.md#, IncrementalExecution>>.
