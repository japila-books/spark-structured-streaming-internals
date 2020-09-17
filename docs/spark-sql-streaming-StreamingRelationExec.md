# StreamingRelationExec Leaf Physical Operator

`StreamingRelationExec` is a leaf physical operator (i.e. `LeafExecNode`) that...FIXME

`StreamingRelationExec` is <<creating-instance, created>> when `StreamingRelationStrategy` spark-sql-streaming-StreamingRelationStrategy.md#apply[plans] `StreamingRelation` and `StreamingExecutionRelation` logical operators.

[source, scala]
----
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
----

[[doExecute]]
`StreamingRelationExec` is not supposed to be executed and is used...FIXME

=== [[creating-instance]] Creating StreamingRelationExec Instance

`StreamingRelationExec` takes the following when created:

* [[sourceName]] The name of a [streaming source](Source.md)
* [[output]] Output attributes
