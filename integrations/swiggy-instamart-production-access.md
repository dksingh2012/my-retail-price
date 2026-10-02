# Swiggy Instamart Production Access — MY RETAIL PRICE

## Integration
- App: MY RETAIL PRICE
- Use case: Gurugram-first grocery price comparison
- Market: Gurugram, PIN 122001
- First target: Swiggy Instamart
- Android package: com.myretailprice.app
- Backend: Cloudflare Worker + D1

## Proposed flow
1. User searches a grocery product in MY RETAIL PRICE.
2. The backend requests permitted Instamart product/availability data through the approved Swiggy integration.
3. MY RETAIL PRICE stores/normalizes only permitted fields needed for comparison.
4. The app shows retailer, product, pack size, price, availability, timestamp and source status.
5. Swiggy prices remain clearly attributed and are never represented as prices from another retailer.

## Current MVP
The app is working with clearly labelled demo data. No live Swiggy credentials are stored in the repository.

## Production application items to prepare
- Company/developer details
- Integration architecture
- Exact HTTPS redirect URI(s), if required by the approved OAuth flow
- Static IP/gateway details if requested
- Security contact
- Data-handling/privacy declaration
- Infrastructure details
- Expected traffic
- Short end-to-end demo video

## Important
Production access is subject to Swiggy review and terms. Do not submit private credentials or tokens to GitHub.
