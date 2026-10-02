# MY RETAIL PRICE — Live Retailer Data Contract

## Purpose
Define the common format used by every permitted retailer source before data reaches the comparison engine.

## Required offer fields
- retailer
- product_id / matched product
- product_name
- brand
- pack
- unit_value
- unit
- price
- mrp (when supplied)
- pincode
- available
- source
- data_status
- updated_at
- product_url (when permitted)

## Comparison rules
1. Match brand + product + pack size before comparing.
2. Never compare different pack sizes as if they were identical.
3. Show item price separately from delivery, handling, small-cart, surge and other platform fees.
4. Use pincode-specific availability.
5. Display the source and freshness timestamp.
6. Sponsored placement must not change the calculated cheapest result.
7. Never label demo/test data as live.
8. Do not scrape private retailer endpoints.

## Production status values
- live_authorized
- staging
- demo
- unavailable
- stale

## Retailer adapters
Each retailer integration should map its permitted response into this common contract. The comparison engine must not depend on retailer-specific field names.

## Swiggy Instamart
Use the approved Swiggy Builders Club/MCP integration after access is granted. Swiggy's current documentation says production access is reviewed and requires a working integration/demo, security and traffic details, and approved scopes.