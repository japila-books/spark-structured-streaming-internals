# StateOperatorProgress

`StateOperatorProgress` is information about updates made to stateful operators in a [StreamingQuery](../StreamingQuery.md) during a trigger:

* <span id="numRowsTotal"> numRowsTotal
* <span id="numRowsUpdated"> numRowsUpdated
* <span id="memoryUsedBytes"> memoryUsedBytes
* <span id="customMetrics"> Custom Metrics (default: empty)

`StateOperatorProgress` is created when `StateStoreWriter` is requested to [getProgress](../physical-operators/StateStoreWriter.md#getProgress).
