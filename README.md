# 🏡 Land Marketplace Backend — No Middleman!

## How it works
- **Sellers** post land with their phone/email directly visible
- **Buyers** search, filter lands and call the owner directly
- **Zero commission** — no broker, no middleman

---

## Tech Stack
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- MySQL 8+
- Lombok

---

## Setup in Eclipse

### Step 1 — Import Project
1. Open Eclipse → `File → Import → Existing Maven Projects`
2. Browse to this folder → Finish
3. Wait for Maven to download all dependencies

### Step 2 — Setup MySQL
```sql
CREATE DATABASE land_marketplace;
```
Update `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3 — Run
Right-click `LandMarketplaceApplication.java` → `Run As → Spring Boot App`

Server starts at: `http://localhost:8080`

---

## API Endpoints

### Auth (Public)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Register as Seller or Buyer |
| POST | `/api/auth/login` | Login, get JWT token |

### Lands (Public - No login needed)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/lands` | All listings |
| GET | `/api/lands/{id}` | Land detail + **Owner phone/email** |
| GET | `/api/lands/search` | Filter by city, price, area, type |

### Lands (Protected - Login required)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/lands` | Post new land (Seller) |
| PUT | `/api/lands/{id}` | Edit listing |
| DELETE | `/api/lands/{id}` | Remove listing |
| PATCH | `/api/lands/{id}/sold` | Mark as sold |
| GET | `/api/lands/my-listings` | My listings |

### Inquiries
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/inquiries/land/{landId}` | Send message to seller |
| GET | `/api/inquiries/received` | Seller: see received inquiries |
| GET | `/api/inquiries/sent` | Buyer: see sent inquiries |

---

## Using JWT in React (Frontend)

```javascript
// 1. Login and save token
const res = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { data } = await res.json();
localStorage.setItem('token', data.token);

// 2. Use token in protected requests
const response = await fetch('http://localhost:8080/api/lands', {
  headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
});
```

---

## Land Types
`AGRICULTURAL`, `RESIDENTIAL`, `COMMERCIAL`, `INDUSTRIAL`, `FOREST`, `PLANTATION`, `OTHER`

## Register Body Example
```json
{
  "fullName": "Ravi Kumar",
  "email": "ravi@example.com",
  "phone": "9876543210",
  "password": "mypassword",
  "role": "SELLER",
  "city": "Chennai",
  "state": "Tamil Nadu"
}
```

## Post Land Example
```json
{
  "title": "5 Acres Fertile Agricultural Land",
  "description": "Good water source, near highway",
  "price": 2500000,
  "address": "Near Bus Stand",
  "city": "Coimbatore",
  "state": "Tamil Nadu",
  "pincode": "641001",
  "areaInAcres": 5.0,
  "landType": "AGRICULTURAL",
  "roadAccess": "STATE_HIGHWAY",
  "waterSource": true,
  "electricity": true
}
```
