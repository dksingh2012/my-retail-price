# MY RETAIL PRICE

**Compare. Save. Shop.**

Gurugram-first Android grocery price comparison app.

## Current MVP
- Android target API 36
- Gurugram / PIN 122001
- 3D digital MY RETAIL PRICE branding
- Cloudflare Worker API
- Cloudflare D1 product + offer database
- Single-item price comparison
- Cheapest-offer result
- **Smart Basket** comparison
- Cheapest split basket calculation
- Cheapest common single-store basket calculation
- Demo seed data for initial UI testing

## Live API
https://my-retail-price-api.dksingh2012.workers.dev

Endpoints:
- /health
- /api/products?q=milk
- /api/sources
- /api/compare?q=milk
- POST /api/basket

Example basket request:

```json
{
  "items": [
    {"q": "milk"},
    {"q": "atta"},
    {"q": "salt"}
  ]
}
```

## Important
The seeded prices are **demo/test data**. They are NOT live retailer prices.

The production version must use retailer-authorized APIs, feeds, affiliate/deep links, or other permitted data sources. Do not scrape private app endpoints.

## Open in Android Studio
1. Open this my-retail-price folder in Android Studio.
2. Allow Gradle sync.
3. Run on an Android emulator or USB-connected Android phone.
4. Search for milk, atta, or another seeded demo product.
5. Test Smart Basket with comma-separated items such as: milk, atta, salt.

## Current backend status
- Cloudflare D1 schema is deployed and seeded for MVP testing.
- Worker exposes `/api/products`, `/api/compare`, `/api/sources`, and `POST /api/basket`.
- All current prices are explicitly marked `data_status: demo`.
- Production retailer data is intentionally blocked until permitted/authorized sources are connected.

## Next
1. Apply for Swiggy Instamart production access.
2. Add authorized feeds/APIs for Blinkit, Zepto, BigBasket and JioMart where available.
3. Add pincode-specific availability and retailer serviceability.
4. Add delivery/platform/small-cart fees separately from item price.
5. Add quantity support and unit-price normalization.
6. Add product matching and retailer buy/deep links where permitted.
7. Prepare Play Console testing and release.

## Google Play
The project targets Android 16 / API 36 for the current 2026 Google Play target requirement.


<!-- UI build trigger: revised demo-matched APK -->
