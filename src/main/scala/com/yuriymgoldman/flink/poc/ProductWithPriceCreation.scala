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

import com.yuriymgoldman.flink.poc.graphite.SimpleGraphiteClient
import com.yuriymgoldman.flink.poc.model._
import com.yuriymgoldman.flink.poc.source._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.util.Collector

/**
  * Outputs a ProductWithPrice as an example of enrichment from two streams.
  *
  * The application receives the stream of
  * Products, Product Prices
  * Result is output to ProductWithPrice Kafka Sink.
  */
object ProductWithPriceCreation {

  /** main() defines and executes the DataStream program */
  def main(args: Array[String]) {

    // set up the streaming execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // use event time for the application
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // configure watermark interval
    env.getConfig.setAutoWatermarkInterval(1000L)

    // ingest streams
    val products: DataStream[Product] = env
      .addSource(new ProductSource)
      .assignTimestampsAndWatermarks(new CreatedAtTimeAssigner[Product])
      .keyBy(_.product_id)

    val productPrices: DataStream[ProductPrice] = env
      .addSource(new ProductPriceSource)
      .assignTimestampsAndWatermarks(new CreatedAtTimeAssigner[ProductPrice])
      .keyBy(_.product_id)

    // assign price to product based on latest price
    // This could be factored out into its own operator or helper
    val pricedProducts: DataStream[ProductWithLatestPrice] = products
      .connect(productPrices)
      .process(new EnrichmentFunction)

    pricedProducts.map(new SinkToGraphite).print()

    // execute application
    env.execute("ProductWithLatestPriceCreation - A multi-stream enrichment example")
  }

  /**
    * Enrichment join implemented with a CoProcessFunction, used to enrich a stream of products with product price data
    */
  class EnrichmentFunction extends CoProcessFunction[Product, ProductPrice, ProductWithLatestPrice] {

    lazy val productState: ValueState[Product] = getRuntimeContext.getState(
      new ValueStateDescriptor[Product]("product", classOf[Product])
    )
    lazy val priceMapState: MapState[Long, ProductPrice] = getRuntimeContext.getMapState(
      new MapStateDescriptor[Long, ProductPrice]("price buffer", classOf[Long], classOf[ProductPrice])
    )

    override def processElement1(product: Product,
                                 context: CoProcessFunction[Product, ProductPrice, ProductWithLatestPrice]#Context,
                                 out: Collector[ProductWithLatestPrice]): Unit = {
      println("Received Product: " + product.toString)
      productState.update(product)
    }

    override def processElement2(price: ProductPrice,
                                 context: CoProcessFunction[Product, ProductPrice, ProductWithLatestPrice]#Context,
                                 out: Collector[ProductWithLatestPrice]): Unit = {
      println("Received Price: " + price.toString)
      val product = productState.value
      if (product != null) {
        out.collect(new ProductWithLatestPrice(product.product_id, product.name, price.amount, product.created_at, price.created_at, price.created_at))
      } else {
        // buffer the price to join it once customer arrives
        priceMapState.put(price.created_at, price)
        context.timerService.registerEventTimeTimer(price.created_at)
      }
    }

    override def onTimer(eventTime: Long,
                         context: CoProcessFunction[Product, ProductPrice, ProductWithLatestPrice]#OnTimerContext,
                         out: Collector[ProductWithLatestPrice]): Unit = {
      val price = priceMapState.get(eventTime)
      if(price != null) {
        priceMapState.remove(eventTime)
        val product = productState.value
        out.collect(new ProductWithLatestPrice(product.product_id, product.name, price.amount, product.created_at, price.created_at, price.created_at))
      }
    }
  }

  /**
   * Send to Graphite (to be replaced with Send to Kafka)
   */
  class SinkToGraphite extends MapFunction[ProductWithLatestPrice, ProductWithLatestPrice] {
    lazy val client = new SimpleGraphiteClient("graphite", 2003);

    override def map(el: ProductWithLatestPrice): ProductWithLatestPrice = {
      client.sendMetric("flink.product_with_latest_price." + el.name, el.amount)
      return el
    }
  }
}
