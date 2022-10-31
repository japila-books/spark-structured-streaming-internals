# Structured Streaming UI

Structured Streaming applications can be monitored using [web UI](StreamingQueryTab.md) that attaches the following two pages:

* [Streaming Query](StreamingQueryPage.md)
* [Streaming Query Statistics](StreamingQueryStatisticsPage.md)

Streaming events are intercepted using [StreamingQueryStatusListener](StreamingQueryStatusListener.md) (and persisted in the [store](StreamingQueryStatusListener.md#store)). The store is used to create a [StreamingQueryStatusStore](StreamingQueryStatusStore.md#store) for [StreamingQueryTab](StreamingQueryTab.md#store) (and the pages).
