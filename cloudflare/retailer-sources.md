# Retailer Data Integration Plan

## Goal
MY RETAIL PRICE must show location-specific grocery prices only from permitted data sources.

## Source status

| Retailer | Integration path | Current status |
|---|---|---|
| Swiggy Instamart | Official Swiggy Builders Club / API | Integration available; production access requires approval |
| Blinkit | Direct partner/feed arrangement | No public consumer price API verified |
| Zepto | Direct partner/feed arrangement | No public developer price API verified |
| BigBasket | Direct partner/feed arrangement | No public consumer price API verified |
| JioMart | Direct partner/feed arrangement | Commercial price/listing collection is restricted by current consumer terms; written authorization required |

## Rules
1. Never scrape private mobile-app endpoints.
2. Never present demo prices as live prices.
3. Every offer should carry a source, timestamp, pincode and data status.
4. Delivery/handling/small-cart/surge charges must be treated separately from item price.
5. Product matching must use brand + product + pack size before comparing prices.
6. Sponsored placements must never be presented as the cheapest price merely because they are sponsored.

## First production integration
Swiggy Instamart is the first official integration target because Swiggy currently provides a developer portal and Builders Club with an Instamart API/MCP route. Production access is reviewed by Swiggy.

## Required production credentials
- Swiggy/Instamart approved integration credentials
- Production redirect URI if OAuth is used
- Any approved scopes
- Approved use case / expected volume

Until approval, the app continues using clearly labelled demo data.


## Non-scraping policy
Current retailer terms and partner materials do not provide a safe basis for scraping private consumer endpoints. MY RETAIL PRICE will only ingest data supplied through an authorized retailer feed/API, a documented partner arrangement, or a licensed data provider whose contract permits this use.
