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
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * Assigns timestamps to PurchaseRequests based on their internal timestamp and
  * emits watermarks with five seconds slack.
  */
class CreatedAtTimeAssigner[T <: CreatedAtEventTimeable]
    extends BoundedOutOfOrdernessTimestampExtractor[T](Time.seconds(5)) {

  override def extractTimestamp(r: T): Long = r.created_at
}
