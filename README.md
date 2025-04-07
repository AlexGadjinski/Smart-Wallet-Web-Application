# 💳 Smart Wallet Web App

A **Smart Wallet** is a browser-based financial management application that allows users to register, manage wallets, perform transactions, subscribe to plans, and track their financial activity through a rich UI powered by Thymeleaf and Spring Boot.

---

## 🔧 Core Functionalities

### 1. User Registration
- Register with username, password, and country.
- Automatically:
  - Creates a default **Wallet** (ACTIVE, €20, EUR).
  - Assigns a **FREE Subscription** (DEFAULT, MONTHLY, €0).
- Ensures secure credential storage and uniqueness.

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
  - Personal data (name, country, balance, contact).
  - Latest transactions.
  - Subscription details.

---

## 💼 Wallet Functionality

### 5. Top-Up Wallet
- Add funds to an ACTIVE wallet.
- Automatically records the transaction.

### 6. Charge Wallet
- Deduct funds for services or subscription upgrades.
- Fails gracefully if balance is insufficient or wallet is INACTIVE.
- All charges (success or failure) are recorded as transactions.

---

## 🧾 Subscriptions

### 7. View Subscriptions
- List of active and historical subscriptions.

### 8. Upgrade Subscription
- Change plan (e.g., to PREMIUM or ULTIMATE).
- Price and renewal eligibility handled by the system.

---

## 💸 Transactions

### 9. Transaction Tracking
- View all personal transactions:
  - Deposits, Withdrawals.
  - Status: SUCCEEDED or FAILED.
  - Timestamps and descriptions.

---

## 📄 Pages (Thymeleaf Views)

| Page                  | Path                          | Description                  |
|-----------------------|-------------------------------|------------------------------|
| **Index**             | `/`                           | Public landing page          |
| **Register**          | `/register`                   | User registration            |
| **Login**             | `/login`                      | User login                   |
| **Home**              | `/home`                       | User dashboard               |
| **Edit Profile**      | `/users/{id}/profile`         | Profile management           |
| **Wallets**           | `/wallets`                    | View wallet(s)               |
| **Subscriptions**     | `/subscriptions`              | Subscription options         |
| **Subscription History** | `/subscriptions/history`  | Past subscriptions           |
| **Transactions**      | `/transactions`               | Transaction history          |
| **Users**             | `/users`                      | Admin/user list view         |
| **Reports**           | `/reports`                    | User financial reports       |

---

## 📁 Tech Stack

- **Java + Spring Boot**
- **Thymeleaf** (templating engine)
- **MySQL** (persistence)
- **HTML/CSS** (custom UI)
- **Maven** (project management)

---

## 🧪 Testing & Initialization

- Register a new user or insert a test user via an initialization script.
- Use the home dashboard to access all core features.
