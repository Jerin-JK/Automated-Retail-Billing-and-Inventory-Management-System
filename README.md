# Supermarket Billing and Inventory Management

Java 17 desktop application using Swing (`JFrame`), JDBC, and local MySQL. Source code is grouped by business module so teammates can work in feature areas.

## Source layout

```text
src/
  Main.java                  Application entry point
  app/ui/                    Main dashboard and module navigation
  auth/                      Login, user authentication, password hashing
    dao/ service/ security/ ui/
  inventory/                 Product catalog and stock management
    dao/ model/ service/ ui/
  billing/                   Checkout, cart, sales persistence
    dao/ model/ service/ ui/
  reports/ui/                Sales summaries and receipt reprinting
  users/ui/                  Admin user management screens
  suppliers/                 Supplier management
    dao/ service/ ui/
  shared/                    Reusable types and app-wide setup/UI helpers
    config/ model/ ui/
```

### Team boundaries

- Assign one teammate to a feature folder such as `inventory`, `billing`, `auth`, or `suppliers`.
- Keep feature screens, business rules, database access, and feature-specific models inside that feature's folder.
- Put code in `shared/` only when multiple modules need it; coordinate changes there with the other module owners.
- `reports` and `users` currently contain UI screens that call the billing and auth services. Add a `service/` or `dao/` subfolder there if those modules gain their own business logic or queries.
- `app/ui/DashboardFrame.java` connects the feature screens. The root `Main.java` launches the app.

## Database setup

1. Install and start MySQL Server locally.
2. In a MySQL client, run `sql/schema.sql`, followed by `sql/seed.sql`. The schema creates the `retail_db` database and its tables.
3. Copy `config/db.properties.example` to `config/db.properties`, then set the correct MySQL user and password. `RETAIL_DB_URL`, `RETAIL_DB_USER`, and `RETAIL_DB_PASSWORD` environment variables can override the file.
4. Keep `config/db.properties` private; it is ignored by Git.

On first launch, the application asks you to create the initial administrator. The password is hashed in Java with PBKDF2 and is not inserted as plaintext. There is no default admin username or password.

## Build and run

Install JDK 17+ and Maven, then run these commands from the project directory:

```powershell
mvn clean compile
mvn exec:java
```

The MySQL Connector/J driver is declared in `pom.xml` and Maven resolves it at build time.

## Main behavior

- Admins can add and edit products, adjust stock with a reason, delete individual products, and deactivate products that are referenced by past bills.
- Cashiers can view inventory and low-stock status, search by product name/SKU/barcode, bill products, record a payment, and print a receipt. They cannot manage inventory, users, suppliers, or reports.
- Sales are saved in one database transaction with line items, payment, stock deductions, and stock movement history.
- Admins can create/disable users, manage supplier records, see daily sales totals and search by date, and double-click a sale to view/reprint its receipt.
- Product categories are entered when adding/editing a product and created automatically if new.

## Suggested follow-up work

The current version is a local single-store baseline. Supplier records are maintained separately and are not yet selectable on product forms. Payment uses one payment method per sale; split tender, refunds/returns, barcode scanner device integration, tax-region configuration, and richer sales/export reports can be added as later increments.
