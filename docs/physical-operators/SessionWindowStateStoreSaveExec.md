# SessionWindowStateStoreSaveExec Physical Operator

`SessionWindowStateStoreSaveExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode/)).

## <span id="shortName"> Short Name

```scala
shortName: String
```

`shortName` is part of the [StateStoreWriter](StateStoreWriter.md#shortName) abstraction.

---

`shortName` is the following text:

```text
sessionWindowStateStoreSave
```

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution`...FIXME
