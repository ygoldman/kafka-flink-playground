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
package com.yuriymgoldman.flink.poc

import java.util.Calendar
import com.yuriymgoldman.flink.poc.graphite.SimpleGraphiteClient

/**
  * Graphite Test
  */
object GraphiteTest {
  def main(args: Array[String]): Unit = {
    val client = new SimpleGraphiteClient("localhost", 2003);

    val i = 0
    for( i <- 1 to 10){
      println(i + ":")
      client.sendMetric("universal.answer", i);
      Thread.sleep(100)
    }

    println("Graphite Test completed")
  }
}
