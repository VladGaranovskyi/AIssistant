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

🛠 AIssistant IT Ticket System API

An IT ticketing system using Spring Boot + MongoDB, with LLM-powered ticket tagging and automatic assignment to IT Assistants based on expertise probability. 🤖✨

## 📦 Models
### User 👤

Represents users of the system.

Fields:

id : ObjectId

username : String

email : String

password : String (hashed, ignored in JSON) 🔒

name : String

bio : String

expertiseTags : Map<String, Double> — tag → probability 📊

tickets : List<Ticket> — solved tickets ✅

roles : Set<Role> — roles of the user (ROLE_USER, ROLE_ASSISTANT, ROLE_ADMIN)

### Ticket 🎫

Represents a ticket submitted by a user.

Fields:

id : ObjectId

headline : String

description : String

date : Date 🗓

issuer : User — who created the ticket 👤

solver : User — assigned IT Assistant (nullable) 🛠

generalCategory : String — main category

tags : List<String> — tags extracted via LLM 🏷

isSolved : Boolean — true if solved ✅

### Role 🏷

Represents a user role.

Fields:

id : ObjectId

name : ERole enum (ROLE_USER, ROLE_ASSISTANT, ROLE_ADMIN)

## 📬 Payloads
AddPersonalDataRequest 📝

Used to update user personal info.

{
  "name": "Vladik",
  "bio": "network and software engineering expert",
  "expertiseTags": {
    "network": 0.9,
    "software": 0.8,
    "hardware": 0.1
  }
}

### TicketCreationRequest 🆕

Used to create a new ticket.

{
  "headline": "Internet is down",
  "description": "Cannot connect to router in office",
  "generalCategory": "network"
}

## 🛠 Controllers & API Endpoints
### UserDataController 👤

POST /api/userdata/add_personal_data ✏️
Update user name, bio, and expertise tags. Requires authentication via JWT cookie.

GET /api/userdata/my_user_data 🔍
Returns logged-in user data.

### TicketController 🎫

POST /api/ticket/create/ticket ➕
Create a new ticket.

Flow:

Verify user is logged in. 🔐

Save ticket to database. 💾

Call TicketMatcherService to assign best-fit assistants using LLM + MongoDB aggregation. 🤖

Return created ticket. ✅

GET /api/ticket/all 📃
Returns all tickets.

GET /api/ticket/{id} 🔎
Returns details of a specific ticket.

PATCH /api/ticket/{id}/mark_solved ✔️
Marks ticket as solved and updates solver data.
