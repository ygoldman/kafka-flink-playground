/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yuriymgoldman.flink.poc.source

import com.yuriymgoldman.flink.poc.model._
import java.util.Calendar
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.scala._
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

import scala.util.Random

/**
  * Flink SourceFunction to generate Product events.
  */
class ProductSource extends SourceFunction[Product] with CheckpointedFunction {

  var isRunning: Boolean = true
  var cnt: Long = _
  var offsetState: ListState[Long] = _

  override def run(ctx: SourceFunction.SourceContext[Product]): Unit = {

    while (isRunning && cnt < 10) {
      ctx.getCheckpointLock.synchronized {
        // increment cnt
        cnt += 1
        ctx.collect(Product(cnt.toString, "product_" + cnt, Calendar.getInstance.getTimeInMillis))
      }
    }
  }

  override def cancel(): Unit = isRunning = false

  override def snapshotState(snapshotCtx: FunctionSnapshotContext): Unit = {
    // remove previous cnt
    offsetState.clear()
    // add current cnt
    offsetState.add(cnt)
  }

  override def initializeState(initCtx: FunctionInitializationContext): Unit = {
    // obtain operator list state to store the current cnt
    val desc = new ListStateDescriptor[Long]("offset", classOf[Long])
    offsetState = initCtx.getOperatorStateStore.getListState(desc)

    // initialize cnt variable from the checkpoint
    val it = offsetState.get()
    cnt = if (null == it || !it.iterator().hasNext) {
      0L
    } else {
      it.iterator().next()
    }
  }
}
