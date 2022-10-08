# StatefulOperatorStateInfo

`StatefulOperatorStateInfo` identifies the state store for a given stateful physical operator:

* [[checkpointLocation]] Checkpoint directory (`checkpointLocation`)
* [[queryRunId]] <<StreamingQuery.md#runId, Run ID>> of a streaming query (`queryRunId`)
* [[operatorId]] Stateful operator ID (`operatorId`)
* [[storeVersion]] <<state-version, State version>> (`storeVersion`)
* [[numPartitions]] Number of partitions

`StatefulOperatorStateInfo` is <<creating-instance, created>> exclusively when `IncrementalExecution` is requested for [nextStatefulOperationStateInfo](../IncrementalExecution.md#nextStatefulOperationStateInfo).

[[toString]]
When requested for a textual representation (`toString`), `StatefulOperatorStateInfo` returns the following:

```text
state info [ checkpoint = [checkpointLocation], runId = [queryRunId], opId = [operatorId], ver = [storeVersion], numPartitions = [numPartitions]]
```

## <span id="state-version"> State Version and Batch ID

When created (when `IncrementalExecution` is requested for the [next StatefulOperatorStateInfo](../IncrementalExecution.md#nextStatefulOperationStateInfo)), a `StatefulOperatorStateInfo` is given a [state version](#storeVersion).

The state version is exactly the [batch ID](../IncrementalExecution.md#currentBatchId) of the [IncrementalExecution](../IncrementalExecution.md).
