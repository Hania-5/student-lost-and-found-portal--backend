# Lost & Found Campus Portal – Backend

Spring Boot REST API for the Lost & Found Campus Portal. It stores lost and found items in MySQL and protects admin actions with a login.

Frontend repository: https://github.com/Hania-5/student-lost-found-portal-forntend

## Features
- Report, list and look up lost and found items (photos are stored with each item)
- Input validation with clear error messages (400) and 404 for missing items
- Admin login with a token: only the admin can **edit**, **mark as claimed** and **delete** items
- CORS enabled so the React frontend can call the API

## Tech Stack
- Java 17 and Spring Boot 3
- Spring Data JPA (Hibernate)
- MySQL

## Requirements
- Java 17 or newer
- MySQL running on port 3306
- Maven (or the included `mvnw` wrapper)

## Setup
1. Create the database in MySQL:
```sql
   CREATE DATABASE lostfounddb;
```
2. Open `src/main/resources/application.properties` and set your own MySQL username and password:
```
   spring.datasource.username=root
   spring.datasource.password=your_password
```
   Tables are created automatically on the first run.
3. Run the app, either from your IDE (run `LostfoundApplication`) or with:
```
   mvnw spring-boot:run
```
4. The API is now available at http://localhost:8080/api/items

## Admin Login (demo)
The demo admin account is set in `application.properties`:
```
admin.username=admin
admin.password=admin123
```
Change these values before using the project anywhere real. Tokens expire after 8 hours and are cleared when the server restarts.

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/items` | Everyone | List all items |
| GET | `/api/items/{id}` | Everyone | Get one item |
| GET | `/api/items/type/{type}` | Everyone | List items by type (LOST or FOUND) |
| POST | `/api/items` | Everyone | Report a new item |
| PUT | `/api/items/{id}` | Admin | Edit an item |
| PUT | `/api/items/{id}/claim` | Admin | Mark an item as claimed |
| DELETE | `/api/items/{id}` | Admin | Delete an item |
| POST | `/api/admin/login` | Everyone | Log in, returns a token |
| POST | `/api/admin/logout` | Admin | Log out |
| GET | `/api/admin/check` | Admin | Check that a token is still valid |

Admin endpoints need the header `Authorization: Bearer <token>`.

### Item fields
`itemName`, `description`, `category`, `location`, `date`, `type` (LOST or FOUND), `status` (OPEN or CLAIMED), `contactInfo`, `image` (optional photo as text)

## Project Structure
```
com.example.lostfound
  controller/   ItemController, AdminController
  model/        Item
  repository/   ItemRepository
  security/     AdminTokenStore
```

## Author
Hania Fathima – B.Tech Data Science, Anurag University