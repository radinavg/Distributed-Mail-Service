# 📧 Distributed Mail Service

## Project Overview
This project implements a basic email system with distributed architecture. The system comprises a message transfer protocol (DMTP), a message access protocol (DMAP), and multithreaded servers for handling email exchange. It is designed as part of the Distributed Systems Lab coursework at TU Wien to demonstrate concepts like socket communication, multithreading, and concurrent programming.

## Key Features
- **Custom Email Protocols:** Implements DMTP for message transfer and DMAP for message access.
- **Server Architecture:**
  - Transfer Server: Routes messages between users and mailbox servers.
  - Mailbox Server: Manages email storage and user data.
  - Monitoring Server: Tracks server usage and message statistics.
- **Multithreaded Design:** Supports concurrent client connections using thread pools.
- **Protocol Simulation:** Use of plain-text TCP communication with tools like `netcat` or PuTTY.

## Tech Stack
- **Language:** Java 11
- **Framework:** Gradle for build and dependency management
- **Libraries:** Standard Java concurrency and networking packages.

## Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/radinavg/Distributed-Mail-Service.git
   ```

2. **Build the Project**
   - Ensure Java 11+ is installed.
   - Use Gradle to compile the project:
     ```bash
     ./gradlew build
     ```

3. **Run the Servers**
   - Start the monitoring server:
     ```bash
     ./gradlew --console=plain run-monitoring
     ```
   - Start the transfer server:
     ```bash
     ./gradlew --console=plain run-transfer
     ```
   - Start the mailbox server:
     ```bash
     ./gradlew --console=plain run-mailbox
     ```

4. **Compile & Test**
   - Compile the project using Gradle:
     ```bash
     ./gradlew assemble
     ```
   - Compile and run the tests:
     ```bash
     ./gradlew build
     ```
   - Run specific test suites:
     ```bash
     ./gradlew test --tests dslab.Lab1Suite
     ```
   - Run individual tests, e.g.:
     ```bash
     ./gradlew test --tests dslab.transfer.TransferServerTest
     ```

5. **Run the Applications**
   - List available tasks:
     ```bash
     ./gradlew tasks --all
     ```
   - Run a specific application, e.g., monitoring server:
     ```bash
     ./gradlew --console=plain run-monitoring
     ```

## Usage Examples
- **✉️ Send Email via DMTP:**
  ```
  begin
  to trillian@earth.planet
  from zaphod@univer.ze
  subject Hello
  data Greetings from Zaphod
  send
  quit
  ```

- **📥 Access Email via DMAP:**
  ```
  login zaphod 12345
  list
  show 1
  delete 1
  logout
  quit
  ```
