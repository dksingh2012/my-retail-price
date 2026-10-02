# Swiggy Instamart Application — MY RETAIL PRICE

## Integration name
MY RETAIL PRICE

## Organization
MY RETAIL PRICE — grocery price comparison app

## Use case
MY RETAIL PRICE helps users compare grocery products available in Gurugram by showing permitted product, availability and price information from participating retailers. The initial market is Gurugram, PIN 122001. The app is designed to help users compare the same brand/product/pack across retailers and identify the lowest item price without misrepresenting retailer data.

## Instamart role
Swiggy Instamart will be one participating retailer/source. Swiggy data will remain attributed to Swiggy and will only be used within the permissions and terms granted for the integration.

## Technical architecture
Android app -> Cloudflare Worker API -> normalized retailer adapter -> Cloudflare D1 -> comparison engine -> Android app.

Current backend:
https://my-retail-price-api.dksingh2012.workers.dev

Current MVP:
- Android package: com.myretailprice.app
- Gurugram / PIN 122001
- single-item comparison
- Smart Basket comparison
- Cloudflare Worker + D1
- demo data clearly marked as demo

## Requested server
Instamart

## OAuth
Implement Swiggy's documented OAuth 2.1 / PKCE flow when staging credentials are provided. Do not commit credentials, tokens or client secrets to GitHub.

## Security
- HTTPS endpoints
- no retailer credentials in Android source
- no credentials in GitHub
- server-side token handling
- rate-limit aware requests
- safe handling of 401/429 responses
- minimum necessary data retention

## Expected initial traffic
Small pilot focused on Gurugram, with traffic limits agreed with Swiggy before launch. Final QPS/order estimates will be supplied during application review.

## Demo video
Record the current end-to-end MVP flow:
1. Open MY RETAIL PRICE.
2. Select Gurugram / PIN 122001.
3. Search "milk".
4. Show comparison results and cheapest result.
5. Run Smart Basket with "milk, atta, salt".
6. Explain that the current values are demo data.
7. Show the backend architecture.

After staging credentials are received, repeat the same flow using staging data.

## Important
This document is an application preparation document. It does not grant access and does not imply Swiggy approval.
