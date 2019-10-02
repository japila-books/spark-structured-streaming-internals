package org.apache.spark.sql

import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

/**
 * Dataset.ofRows is private[sql]
 * and we need it to create a streaming DataFrame
 */
object UsePrivateSqlHack {

  def ofRows(sparkSession: SparkSession, logicalPlan: LogicalPlan): DataFrame = {
    Dataset.ofRows(sparkSession, logicalPlan)
  }

}