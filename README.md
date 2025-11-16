> ⚠️ **Project Status: In Development**
> 
> This Smart Wallet app is still a **work in progress**. Core features are being implemented and polished. Expect changes, improvements, and additions!

# 💳 Smart Wallet Web App

A **Smart Wallet** is a browser-based financial management application that allows users to register, manage wallets, perform transactions, **send money to other users**, subscribe to plans, and track their financial activity through a rich UI powered by Thymeleaf and Spring Boot.  
**Admin users** can monitor platform usage, view all registered users, and access financial reports.

---

## 🔧 Core Functionalities

### 1. User Registration
- Register with username, password, and country.
- Automatically:
  - Creates a default **Wallet** (ACTIVE, €20, EUR).
  - Assigns a **FREE Subscription** (DEFAULT, MONTHLY, €0).
- Ensures secure credential storage and unique accounts.

### 2. User Login
- Authenticates users with secure password validation.
- Provides access to personal dashboard and financial features.

---

## 👤 User Features

### 3. Profile Management
- View and edit personal information.
- Upload a profile picture.

### 4. Home Dashboard
- Displays:
  - Personal data (name, country, balance, contact)
  - Latest transactions
  - Subscription details

---

## 💼 Wallet Functionality

### 5. Top-Up Wallet
- Add funds to an ACTIVE wallet.
- Automatically records the transaction.

### 6. Charge Wallet
- Deduct funds for services or subscription upgrades.
- Fails gracefully if balance is insufficient or wallet is INACTIVE.
- All charges (success or failure) are recorded as transactions.

### 7. **Send Money (Transfers)**
- Transfer funds to another registered user.
- Creates transactions for both sender and receiver with SUCCESS/FAIL status.

---

## 🧾 Subscriptions

### 8. View Subscriptions
- View current and past subscriptions.

### 9. Upgrade Subscription
- Change plan (e.g., PREMIUM or ULTIMATE).
- Price and renewal logic handled by system.
- Transaction generated automatically upon upgrade.

---

## 💸 Transactions

### 10. Transaction Tracking
- View all personal transactions:
  - Deposits, Withdrawals, Transfers
  - Status: SUCCEEDED or FAILED
  - Timestamps & descriptions

---

## 🛠️ Admin Features

### 11. User Management (Admin Only)
- View all users registered in the system.
- Change user roles (e.g., USER ↔ ADMIN).
- Activate or deactivate user accounts.

### 12. Reports (Admin Only)
- Access financial and user activity reports for platform oversight.

---

## 📄 Pages (Thymeleaf Views)

| Page                      | Path                             | Description                              |
|---------------------------|----------------------------------|------------------------------------------|
| **Index**                 | `/`                              | Public landing page                       |
| **Register**              | `/register`                      | User registration                         |
| **Login**                 | `/login`                         | User login                                |
| **Home**                  | `/home`                          | User dashboard                            |
| **Edit Profile**          | `/users/{id}/profile`            | Profile management                        |
| **Wallets**               | `/wallets`                       | View wallet(s) & wallet actions           |
| **Transfers**             | `/transfers`                     | Send money to other users                 |
| **Subscriptions**         | `/subscriptions`                 | Subscription options & upgrade            |
| **Subscription History**  | `/subscriptions/history`         | Past subscriptions                        |
| **Transactions**          | `/transactions`                  | Transaction history                       |
| **Transaction Details**   | `/transactions/{id}`             | View a single transaction result          |
| **Users (Admin)**         | `/users`                         | View all users & manage roles/status      |
| **Reports (Admin)**       | `/reports`                       | Financial and user activity reports       |

---

## 📁 Tech Stack

- **Java + Spring Boot**
- **Thymeleaf** (templating engine)
- **MySQL** (persistence)
- **HTML/CSS** (custom UI)
- **Maven** (project management)
- **Spring Security** (authentication & authorization)

---

## 🧪 Testing & Initialization

- Register a new user.
- Use the dashboard to explore wallet actions, subscriptions, transactions, and money transfers.
