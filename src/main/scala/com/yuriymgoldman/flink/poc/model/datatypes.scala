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
package com.yuriymgoldman.flink.poc.model

sealed abstract trait CreatedAtEventTimeable { def created_at: Long }
case class Product(product_id: String, name: String, created_at: Long) extends CreatedAtEventTimeable
case class ProductPrice(product_id: String, amount: Double, created_at: Long) extends CreatedAtEventTimeable
case class ProductWithLatestPrice(product_id: String, name: String, amount: Double, product_created_at: Long, price_created_at: Long, created_at: Long) extends CreatedAtEventTimeable
case class TaxRate(country_code: String, amount: Double, created_at: Long) extends CreatedAtEventTimeable
case class PurchaseRequest(product_id: String, country_code: String, created_at: Long) extends CreatedAtEventTimeable
case class PurchaseOrder(product_id: String, product_name: String, country_code: String, tax_rate: Double, amount_before_tax: Double, amount_after_tax: Double, created_at: Long) extends CreatedAtEventTimeable
case class SalesLedger(product_id: String, country_code: String, total_amount: Double, created_at: Long) extends CreatedAtEventTimeable
