# StreamingRelationExec Leaf Physical Operator

`StreamingRelationExec` is a leaf physical operator (i.e. `LeafExecNode`) that...FIXME

## Creating Instance

`StreamingRelationExec` takes the following when created:

* [[sourceName]] The name of a [streaming source](../Source.md)
* [[output]] Output attributes

`StreamingRelationExec` is created when [StreamingRelationStrategy](../execution-planning-strategies/StreamingRelationStrategy.md) execution planning strategy is executed (to plan `StreamingRelation` and `StreamingExecutionRelation` logical operators).

## Demo

```text
scala> spark.version
res0: String = 2.3.0-SNAPSHOT

val rates = spark.
  readStream.
  format("rate").
  load

// StreamingRelation logical operator
scala> println(rates.queryExecution.logical.numberedTreeString)
00 StreamingRelation DataSource(org.apache.spark.sql.SparkSession@31ba0af0,rate,List(),None,List(),None,Map(),None), rate, [timestamp#0, value#1L]

// StreamingRelationExec physical operator (shown without "Exec" suffix)
scala> rates.explain
== Physical Plan ==
StreamingRelation rate, [timestamp#0, value#1L]
```
