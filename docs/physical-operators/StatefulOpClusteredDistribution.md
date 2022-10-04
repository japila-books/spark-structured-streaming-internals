# StatefulOpClusteredDistribution

`StatefulOpClusteredDistribution` is a `Distribution` ([Spark SQL]({{ book.spark_sql }}/physical-operators/Distribution)).

`StatefulOpClusteredDistribution` requires the [Expressions](#expressions) are specified or throws an exception:

```text
The expressions for hash of a StatefulOpClusteredDistribution should not be Nil.
An AllTuples should be used to represent a distribution that only has a single partition.
```

## Creating Instance

`StatefulOpClusteredDistribution` takes the following to be created:

* <span id="expressions"> `Expression`s ([Spark SQL]({{ book.spark_sql }}/expressions/Expression))
* [Required number of partitions](#_requiredNumPartitions)

`StatefulOpClusteredDistribution` is created when:

* `StatefulOperatorPartitioning` is requested to [getCompatibleDistribution](../stateful-stream-processing/StatefulOperatorPartitioning.md#getCompatibleDistribution)
* `StreamingSymmetricHashJoinExec` is requested for the [required child output distribution](../physical-operators//StreamingSymmetricHashJoinExec.md#requiredChildDistribution)

## <span id="_requiredNumPartitions"><span id="requiredNumPartitions"> Required Number of Partitions

`StatefulOpClusteredDistribution` is given a required number of partitions when [created](#creating-instance).

`requiredNumPartitions` is part of the `Distribution` ([Spark SQL]({{ book.spark_sql }}/physical-operators/Distribution/#requiredNumPartitions)) abstraction.

## <span id="createPartitioning"> Partitioning

```scala
createPartitioning(
  numPartitions: Int): Partitioning
```

`createPartitioning` is part of the `Distribution` ([Spark SQL]({{ book.spark_sql }}/physical-operators/Distribution/#createPartitioning)) abstraction.

---

`createPartitioning` asserts that the given `numPartitions` is exactly the [required number of partitions](#_requiredNumPartitions) or throws an exception otherwise:

```text
This StatefulOpClusteredDistribution requires [requiredNumPartitions] partitions,
but the actual number of partitions is [numPartitions].
```

`createPartitioning` creates a `HashPartitioning` ([Spark SQL]({{ book.spark_sql }}/expressions/HashPartitioning)) (with the [expressions](#expressions) and the [numPartitions](#numPartitions)).
