# POC 1

Products
  - product_id
  - name
  - event_time

ProductPrices
  - product_id
  - amount
  - event_time

TaxRates
  - country_code
  - tax_rate
  - event_time

PurchaseRequest
  - product_id
  - country_code
  - event_time

PurchaseOrder
  - product_id
  - product_name
  - country_code
  - tax_rate
  - amount_before_tax
  - amount_after_tax
  - event_time

SalesLedger
  - product_id
  - country_code
  - total_amount
  - event_time
