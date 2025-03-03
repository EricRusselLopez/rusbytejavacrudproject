# 🚀 RusByte Net - User Management System

**👨‍💻 Developer:** Eric Russel M. Lopez

The **RusByte Net - User Management System** is an extension of my **RusByte Net project** where I applied my skills in Web Development, security, simple UI development, and ReactJS skills within Java programming.

You can explore the platform here: [RusByte Net Main Platform](https://rusbyte-betatest.vercel.app/)

## ✨ Overview

This system allows administrators to:

- **Register** new admin user
- **Authenticate** admin users securely
- **Manage** (CRUD) member accounts on the RusByte Net platform

> **Note:** This system only **simulates** data from the RusByte Net platform and does not currently pull real user data. ^\_^

---

## ✨ Features

### 🔄 **App Router** (Dynamic Page Routing)

- Inspired by **React Router**
- Switches between pages dynamically without reloading the entire application (like ReactJS).

### 🔐 **Persistent User Sessions**

- Keeps admin users logged in even after closing the application.
- Uses **session data stored in a file** (similar to **localStorage & Cookies** in web applications).

### 👤 **Complete User Management (CRUD)**

- Admins can **Create, Read, Update, and Delete users** easily through an interactive UI.

### 🔒 **Secure Authentication & Action Verification**

- Users log in with **email & password**.
- Failed login attempts penalty countdown.
- Admins must **re-enter their password before executing sensitive CRUD actions** to prevent unauthorized modifications.

---

## ▶️ Running the Application

## 🔹 Option 1: Running the Compiled JAR File

This is have the `.jar` file, follow these steps:

1️⃣ **Download and locate the JAR file**.  
2️⃣ **Double-click** the file to run the application.  
3️⃣ If it doesn’t open, use **Command Prompt/Terminal**:

```sh
java -jar rusbytejavacrudproject.jar
```

4️⃣ The application should now launch successfully.

---

## 🔹 Option 2: Running from .bat file (Windows Only)

### Step 1: Locate the run.bat File

### Step 2: Double-Click to Run

- This will automatically execute the Java program.

## 🔹 Option 3: Running from Source Code (For Developers)

If running from source, follow these steps:

### Step 1: Install Java

- Check if Java is installed:
  ```sh
  java -version
  javac -version
  ```
- If missing, download Java from [Oracle](https://www.oracle.com/java/) or [OpenJDK](https://openjdk.org/).

### Step 2: Navigate and open the Project Folder to your prefferred IDE (Ex: VSCode)

- Open **Command Prompt/Terminal** and use `cd` to go to the project folder:
  ```sh
  cd path/to/project
  ```

### Step 3: Run the Application

- Execute the main class:
  ```sh
  java Main.java
  ```
- Or simply click the run debugger for java ^^

---

# 🛠️ Troubleshooting

🚨 **App not opening?**

- Ensure Java is installed (`java -version`).
- Try running via **Command Prompt/Terminal** (`java -jar RusByteNetUserManagement.jar`).

🔑 **Permission Issues?**

- Run the command prompt/terminal as **Administrator (Windows)** or **root (Linux/macOS)**.

🔄 **Session Not Persisting?**

- Ensure the user data file (`manageableUsers.txt`) is accessible.

---
