# FreelanceLK Backend

## Listings API (M3)

Base URL: `/api/listings`

Swagger UI: `/swagger-ui/index.html`

### Create Gig

```bash
curl -X POST "http://localhost:8080/api/listings/gigs" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Logo Design Service",
    "description": "Professional logo design with source files and brand guide.",
    "category": "Design",
    "pricingModel": "FIXED",
    "basePrice": 500,
    "deliveryDays": 4
  }'
```

### Create Job

```bash
curl -X POST "http://localhost:8080/api/listings/jobs" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Frontend React Contractor",
    "description": "Looking for a React developer with API integration experience.",
    "employmentType": "CONTRACT",
    "hourlyRate": 1500,
    "location": "Colombo",
    "isRemote": true
  }'
```

### List Listings (Filters)

```bash
curl "http://localhost:8080/api/listings?type=GIG&category=Design&keyword=logo&minPrice=100&maxPrice=1000&page=0&size=10"
```

### Get Listing by ID

```bash
curl "http://localhost:8080/api/listings/<LISTING_ID>"
```

### Update Listing

```bash
curl -X PUT "http://localhost:8080/api/listings/<LISTING_ID>" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Logo Design Package",
    "description": "Updated premium logo design with brand kit.",
    "category": "Design",
    "pricingModel": "FIXED",
    "basePrice": 800,
    "deliveryDays": 3
  }'
```

### Delete Listing

```bash
curl -X DELETE "http://localhost:8080/api/listings/<LISTING_ID>" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
