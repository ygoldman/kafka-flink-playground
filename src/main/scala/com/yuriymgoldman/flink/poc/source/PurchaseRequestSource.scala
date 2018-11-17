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
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

import scala.util.Random

/**
  * Flink SourceFunction to generate PurchaseRequest
  *
  * Each parallel instance of the source simulates a PurchaseRequest for a random product
  * every 2s.
  *
  * Note: This is a simple data-generating source function that does not checkpoint its state.
  * In case of a failure, the source does not replay any data.
  */
class PurchaseRequestSource extends RichParallelSourceFunction[PurchaseRequest] {

  // flag indicating whether source is still running.
  var running: Boolean = true

  /** run() continuously emits PurchaseRequest by emitting them through the SourceContext. */
  override def run(ctx: SourceContext[PurchaseRequest]): Unit = {

    // initialize random number generator
    val rand = new Random()
    // look up index of this parallel task
    val productId = 1 + rand.nextInt(10)

    // emit data until being canceled
    while (running) {
      // emit new ProductPrice
      ctx.collect(PurchaseRequest(productId.toString, if (productId % 2 == 0) "usa" else "gbr", Calendar.getInstance.getTimeInMillis))

      // wait for 2 seconds
      Thread.sleep(2000)
    }
  }

  /** Cancels this SourceFunction. */
  override def cancel(): Unit = {
    running = false
  }
}
