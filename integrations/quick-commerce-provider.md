# Quick-Commerce Provider Integration

## Candidate provider
QuickCommerce API currently documents a single API for product search/item lookup across BlinkIt, Zepto, Swiggy, BigBasket and JioMart, with location parameters and fields including price, MRP, availability and deeplink.

Official provider documentation:
https://quickcommerceapi.com/docs

## Important commercial requirement
Before MY RETAIL PRICE displays or monetizes third-party retailer data, confirm in the provider contract/terms that the plan explicitly permits:
- commercial use
- price comparison
- displaying retailer names/prices
- displaying retailer product/deep links
- caching/retaining permitted fields
- Gurugram/PIN-level location queries

Do not assume API availability alone grants these rights.

## MY RETAIL PRICE adapter contract

Input:
- q
- pincode
- latitude/longitude when required
- retailer/platform

Normalized output:
- retailer
- external_item_id
- name
- brand
- pack
- price
- mrp
- available
- pincode
- product_url
- source
- data_status
- fetched_at

## Recommended rollout
1. Create provider account.
2. Verify Gurugram / PIN 122001 coverage.
3. Obtain API key.
4. Confirm commercial price-comparison rights.
5. Test 10 common products.
6. Compare returned products against our brand + pack matching.
7. Connect provider to Cloudflare Worker as a server-side secret.
8. Keep demo data as fallback until live data passes validation.
9. Enable live mode only after validation.

## Security
The provider API key must never be placed in the Android app or GitHub repository. Store it as a Cloudflare Worker secret.

## Current status
MY RETAIL PRICE backend already has the normalized offer schema and protected import endpoint. The provider adapter can now be added without changing the Android UI.
