## 🔐 Authentication & Role Management

This application implements **JWT-based authentication** with **role-based access control** using Spring Security and MongoDB.

---

### 🧱 Technologies Used

- **Spring Security**
- **JWT (JSON Web Token)**
- **MongoDB**
- **Spring Boot**
- **Lombok**

---

### 🔑 User Authentication Flow

1. **Signup**
   - Users register via `/api/auth/signup`.
   - During signup, they are assigned a **default role** (`ROLE_USER`) unless otherwise specified.
   - The `User` object stores email, password (hashed), and a set of roles.

2. **Login**
   - Users authenticate via `/api/auth/signin`.
   - If credentials are valid, the server:
     - Generates a **JWT** using `JwtUtils`.
     - Stores the JWT in an **HTTP-only cookie** (configured as `aissistant_id`).
     - Returns user info (roles, email, etc.) in the response body.

3. **JWT Handling**
   - The `JwtTokenFilter` extracts and validates the JWT from incoming requests.
   - If valid, Spring Security sets the user in the context for authorization.

---

### 🧾 Role Management

- Roles are stored in the **MongoDB `roles` collection**.
- Defined in the `ERole` enum:
  ```java
  public enum ERole {
      ROLE_USER,
      ROLE_ASSISTANT,
      ROLE_ADMIN
  }
