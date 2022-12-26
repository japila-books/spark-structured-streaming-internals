# Testing Streaming Queries

Spark Structured Streaming comes with the built-in [Memory Data Source](datasources/memory/index.md) for writing tests of streaming queries.

The idea is to load data from a [memory source](datasources/memory/MemoryStreamBase.md), execute data stream transformations (your code), and write the result out to a [memory sink](datasources/memory/MemorySink.md) (that becomes a queryable temporary view).

## Demo

!!! tip
    Review [MemorySinkSuite]({{ spark.github }}/sql/core/src/test/scala/org/apache/spark/sql/execution/streaming/MemorySinkSuite.scala#L31) and [FileStreamSourceSuite]({{ spark.github }}/sql/core/src/test/scala/org/apache/spark/sql/streaming/FileStreamSourceSuite.scala#L227) test suites in the source code of Apache Spark.

## Learn More

* [How to perform Unit testing on Spark Structured Streaming?](https://stackoverflow.com/q/56894068/1305344)
* [Unit Testing Spark Structured Streaming Application using Memory Stream](https://blog.devgenius.io/unit-testing-spark-structured-streaming-application-using-memory-stream-fbaebfd39791)
* [Integration tests and Structured Streaming](https://www.waitingforcode.com/apache-spark-structured-streaming/integration-tests-structured-streaming/read)
* [Writing Unit Test for Apache Spark using Memory Streams](https://blog.knoldus.com/apache-sparks-memory-streams/)
