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
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

import scala.util.Random

/**
  * Flink SourceFunction to generate TaxRate events.
  */
class TaxRateSource extends SourceFunction[TaxRate] {

  var isRunning: Boolean = true

  override def run(ctx: SourceFunction.SourceContext[TaxRate]): Unit = {
    if (isRunning) {
      val eventTime = Calendar.getInstance.getTimeInMillis
      ctx.collect(TaxRate("usa", 8.85, eventTime))
      ctx.collect(TaxRate("gbr", 0.0, eventTime))
    }
  }

  override def cancel(): Unit = isRunning = false
}
