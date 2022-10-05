# Structured Streaming Web UI

Spark Structured Streaming can be monitored using [StreamingQueryTab](StreamingQueryTab.md) that attaches the following two pages:

* [StreamingQueryPage](StreamingQueryPage.md)
* [Statistics](StreamingQueryStatisticsPage.md)

Streaming events are intercepted using [StreamingQueryStatusListener](StreamingQueryStatusListener.md) (and persisted in the [store](StreamingQueryStatusListener.md#store)). The store is used to create a [StreamingQueryStatusStore](StreamingQueryStatusStore.md#store) for [StreamingQueryTab](StreamingQueryTab.md#store) (and the two attached pages).
