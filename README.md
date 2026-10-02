# MY RETAIL PRICE

**Compare. Save. Shop.**

Gurugram-first Android grocery price comparison app.

## Current MVP
- Android target API 36
- Gurugram / PIN 122001
- 3D digital MY RETAIL PRICE branding
- Cloudflare Worker API
- Cloudflare D1 product + offer database
- Product comparison endpoint
- Cheapest-offer result
- Demo seed data for initial UI testing

## Live API
https://my-retail-price-api.dksingh2012.workers.dev

Endpoints:
- /health
- /api/products?q=milk
- /api/compare?q=milk

## Important
The seeded prices are **demo/test data**. They are NOT live retailer prices.

The production version must use retailer-authorized APIs, feeds, affiliate/deep links, or other permitted data sources. Do not scrape private app endpoints.

## Open in Android Studio
1. Open this my-retail-price folder in Android Studio.
2. Allow Gradle sync.
3. Run on an Android emulator or USB-connected Android phone.
4. Search for milk, atta, or another seeded demo product.

## Google Play
The project targets Android 16 / API 36 for the current 2026 Google Play target requirement.
