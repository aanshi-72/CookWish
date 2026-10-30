# CookWish — React + JAX-RS/JAXB + MySQL

A starter full-stack recipe application with:
- React + Vite frontend
- Axios for API calls
- JAX-RS (Jersey 2) REST API
- JAXB-annotated recipe model and XML endpoints for create/read integration
- MySQL database
- JWT login with `USER` and `ADMIN` roles
- Admin recipe create/update/delete; normal users can only view/search recipes
- Image URLs stored with recipes

## Demo accounts
- Admin: `admin` / `Admin@123`
- Normal user: `user` / `User@123`

Change these demo credentials and the JWT secret before real deployment. This starter uses demo in-memory credentials for authentication; recipes are stored in MySQL.

## Prerequisites
- Node.js 18+ and npm
- JDK 11
- Maven
- Apache Tomcat 9 (not Tomcat 10/11, because this project uses `javax.*`)
- MySQL Server + MySQL Workbench
- VS Code (frontend) and Spring Tool Suite / Eclipse (backend)

## 1. Create the database
Open MySQL Workbench and run `backend/database/cookwish.sql`.
It creates `cookwish_db` and a `recipes` table with sample recipes.

## 2. Configure backend database
Edit `backend/src/main/resources/db.properties` and set your MySQL username/password.

## 3. Run backend
From a terminal in `backend`:
```bash
mvn clean package
```
Copy `target/cookwish.war` to Tomcat's `webapps` folder and start Tomcat.
Base API URL: `http://localhost:8080/cookwish/api`

If your Tomcat context path differs, update `frontend/.env` accordingly.
To import in Eclipse/STS: File → Import → Existing Maven Projects → select the `backend` folder.
Then use `mvn clean package` or configure a Tomcat 9 server in the IDE.

## 4. Run frontend
In VS Code, open the `frontend` folder:
```bash
npm install
npm run dev
```
Open the local URL Vite prints (normally `http://localhost:5173`).

## Login and permissions
- Login as `user` to view and search recipes. Admin controls are hidden and API writes are rejected.
- Login as `admin` to access the centered recipe form and perform create/update/delete.
- The navbar Home item returns to the recipe listing.
- Logout clears the token and opens the Logout page; use its button to return to Login.

## Architecture notes
- JAX-RS handles REST routing, HTTP methods, auth, and JSON API responses.
- `Recipe` is JAXB-annotated. `/api/recipes/xml` demonstrates JAXB XML serialization for read, and `/api/recipes/xml` POST demonstrates JAXB XML deserialization for create. These XML endpoints are for XML clients; the React app uses JSON endpoints through Axios.
- The requested “JAXB for create/read, JAX-RS for the rest” is implemented by using JAXB XML for XML create/read routes, while JAX-RS exposes and routes all HTTP endpoints. JAXB is an XML binding API, not a standalone REST framework.
- JWT is implemented with a small HMAC-SHA256 helper to keep the project easy to inspect. Do not use this demo as-is for production: use a maintained JWT library, hashed passwords, environment-managed secrets, HTTPS, validation, and database-backed users.
- Recipe images use URL strings. For a real upload workflow, add multipart upload and file storage.
