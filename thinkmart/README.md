# Think Mart - Spring Boot + MySQL e-commerce

Your original HTML/CSS frontend, now connected to a real backend.
One Spring Boot app serves both the website and the REST API, so there is nothing else to host.

## What you need
- JDK 17 or newer
- Maven 3.9+ (or open the folder in IntelliJ / Eclipse / VS Code, which run Maven for you)
- MySQL 8 running locally

## Run it (3 steps)
1. **Start MySQL.** The database `thinkmart` is created automatically.
2. **Set your MySQL password** - edit `src/main/resources/application.properties`
   (`spring.datasource.username/password`) *or* set the environment variables `DB_USERNAME` and `DB_PASSWORD`.
   Defaults are `root` / `root`.
3. **Start the app**
   ```
   mvn spring-boot:run
   ```
   Open **http://localhost:8080**

Or with Docker (starts MySQL too): `docker compose up --build`

## Logins
| Role | Email | Password |
|------|-------|----------|
| Admin (created on first start) | `admin@thinkmart.com` | `Admin@123` |
| Customer / Vendor | register on the Register page | your choice |

Change the admin password and `JWT_SECRET` before putting the site online.

## What works
- **Register / Login** (BCrypt passwords, JWT token, profile picture upload)
- **Products** loaded from MySQL, with search, category filter and sorting
- **Cart** saved per user in the database (add, +/-, remove, live total, stock check)
- **Checkout** creates an order, reduces stock and empties the cart
- **My Orders** page
- **Contact form** saves messages to the database
- **Dashboard** (Vendor / Admin): add, edit, delete products.
  Admin also sees all orders (change status; cancelling restores stock) and customer messages.
  Vendors only see and change their own products.

Products photos: copy your `.jpg` files into `src/main/resources/static/images/`
named exactly as listed in `images/PUT-IMAGES-HERE.txt` (e.g. `vivo-v29.jpg`).
Until then a neat placeholder is shown.

## Changes I made to your original pages
- Login now uses **Email** instead of Username (the Register form has no username field).
- The **Admin** radio was removed from Register - anyone could have made themselves admin.
  The admin account is created by the server on first start instead.
- Password max length raised from 8 to 20 characters.
- Cart, product cards and checkout are now generated from the API instead of hard-coded HTML.
- New pages: `Orders.html` and `Admin.html` (Dashboard). New file: `js/app.js` (all frontend logic).

## API summary
| Method | Path | Who |
|--------|------|-----|
| POST | `/api/auth/register` (multipart) , `/api/auth/login` | public |
| GET | `/api/auth/me` | logged in |
| GET | `/api/products?category=&q=` , `/api/products/{id}` , `/api/products/categories` | public |
| GET | `/api/products/mine` | vendor / admin |
| POST / PUT / DELETE | `/api/products` , `/api/products/{id}` | vendor (own) / admin |
| GET / POST | `/api/cart` | logged in |
| PUT / DELETE | `/api/cart/{productId}` | logged in |
| POST | `/api/orders/checkout` | logged in |
| GET | `/api/orders/my` | logged in |
| GET | `/api/admin/orders` , `/api/admin/messages` | admin |
| PUT | `/api/admin/orders/{id}/status` | admin |
| POST | `/api/contact` | public |

Send the token as `Authorization: Bearer <token>`.

## Deploying
Anything that runs a Java 17 app and a MySQL database works (Render, Railway, AWS, a VPS...).
Use the `Dockerfile`, and set these environment variables:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`,
and `UPLOAD_DIR` (point it at a persistent disk, or profile pictures are lost on redeploy).
`PORT` is picked up automatically.

## Project layout
```
src/main/java/com/thinkmart
  config/      WebConfig (CORS, uploads, BCrypt), DataInitializer (admin + 12 products)
  security/    JwtUtil, AuthInterceptor (checks the token on /api/**), Access (role check)
  model/       User, Product, CartItem, CustomerOrder, OrderItem, ContactMessage
  repository/  Spring Data JPA repositories
  service/     business logic (cart, checkout, products, auth, file upload)
  controller/  REST endpoints
src/main/resources
  application.properties
  static/      your frontend (Pages/, styles/, js/app.js, images/, bg-video.mp4)
```
