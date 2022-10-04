# Structured Streaming Web UI

Spark Structured Streaming can be monitored using web UI in [StreamingQueryTab](StreamingQueryTab.md) (with [spark.sql.streaming.ui.enabled](../configuration-properties.md#spark.sql.streaming.ui.enabled) enabled).

Streaming events are intercepted using [StreamingQueryStatusListener](StreamingQueryStatusListener.md) (and persisted in the [store](StreamingQueryStatusListener.md#store)). The store is used to create a [StreamingQueryStatusStore](StreamingQueryStatusStore.md#store) for [StreamingQueryTab](StreamingQueryTab.md#store).
