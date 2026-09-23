# Nextwise
SupportDesk --- Customer Support Ticketing System
> A lightweight full-stack customer support platform for raising,
> managing, tracking, and resolving support tickets.
1. Project Overview
SupportDesk is a customer support ticketing system developed as a
full-stack application. It provides separate experiences for customers
and support agents, allowing support requests to move from creation
through assignment, conversation, status updates, resolution, and
closure.
The project is designed around the core workflow:
Requirement → Design → Frontend → REST API → Authentication → Database
→ Ticket Management → Testing → Delivery
The application follows a layered architecture so that the React
frontend communicates with the Spring Boot backend through REST APIs,
while the backend handles authentication, authorization, validation,
business logic, and MySQL persistence.
---
2. Problem Statement
Customer support teams need a structured way to receive and manage
customer issues.
Without a centralized ticketing system, support requests can become
difficult to track, ownership can be unclear, and conversations may be
scattered across different channels.
SupportDesk solves this problem by providing a single platform where:
Customers can
Create an account.
Log in securely.
Create support tickets.
Select a category and priority.
View their own tickets.
Search and filter tickets.
Open ticket details.
View the conversation history.
Reply to support agents.
Track ticket status.
Close a ticket after it has been resolved.
Support Agents can
Register through the authorized agent registration flow.
Log in securely.
View organization-wide ticket statistics.
View all tickets.
Search and filter tickets.
View urgent tickets.
View unassigned tickets.
Assign tickets to agents.
Take ownership of tickets.
Change ticket status.
Change ticket priority.
Reply to customers.
Add internal notes.
Resolve and close tickets.
---
3. Approach Followed
The project was developed using a client-server architecture with a
clear separation between presentation, API, business logic, security,
and data access.
Overall architecture
``` text
                    SupportDesk
                         |
             +-----------+-----------+
             |                       |
          Frontend                Backend
       React + Vite           Spring Boot 3.3.4
             |                       |
        React Router             REST APIs
             |                       |
           Axios              Security + JWT
             |                       |
             +----------->   Service Layer
                                  |
                           Repository Layer
                                  |
                               JPA/Hibernate
                                  |
                                MySQL
```
Frontend approach
The frontend is built using React with TypeScript and Vite.
The UI is divided into reusable:
Pages
Layouts
Components
API modules
Authentication context
Route guards
Shared UI elements
The frontend communicates with the backend through Axios REST requests.
Backend approach
The backend follows a layered Spring Boot architecture:
``` text
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```
Security is handled separately through Spring Security and JWT
authentication.
Database approach
The system uses MySQL with JPA/Hibernate.
The main domain entities are:
``` text
User
  |
  +---- Customer
  |
  +---- Support Agent

User
  |
  +---- Ticket
           |
           +---- Ticket Message
```
---
4. Main Features
Authentication
Customer registration.
Service-agent registration.
Login.
JWT token generation.
Current-user retrieval.
Password hashing with BCrypt.
Protected API routes.
Protected frontend routes.
Role-based authorization.
Validation and error responses.
User Roles
The backend supports:
`ROLE_CUSTOMER`
`ROLE_AGENT`
`ROLE_SUPPORT_AGENT`
`ROLE_ADMIN`
The main application workflow focuses on customers and support agents.
Customer permissions
Customers can:
Create tickets.
View their own tickets.
View their own conversations.
Reply to their tickets.
Close resolved tickets.
Customers cannot:
View another customer's ticket.
Assign tickets.
Change ticket priority.
Support Agent permissions
Support agents can:
View organization-wide tickets.
Assign tickets.
Change priority.
Change status.
Reply to customers.
Add internal notes.
Manage ticket queues.
---
5. Ticket Management
Each ticket contains:
Ticket number.
Title.
Description.
Category.
Priority.
Status.
Customer.
Assigned agent.
Created timestamp.
Updated timestamp.
Resolved timestamp.
Ticket Categories
The current application supports:
Technical
Billing
Account
General
Feature Request
Ticket Priorities
Low
Medium
High
Urgent
Ticket Statuses
Open
In Progress
Resolved
Closed
Ticket lifecycle
``` text
OPEN
  ↓
IN PROGRESS
  ↓
RESOLVED
  ↓
CLOSED
```
A customer reply to a resolved or closed ticket automatically moves the
ticket back to In Progress, allowing the conversation to continue.
---
6. Ticket Conversation
Every ticket can contain a persistent conversation.
Each message stores:
Sender.
Ticket.
Message content.
Internal-note flag.
Timestamp.
Support agents can create internal notes.
Customers only receive non-internal conversation messages.
This provides a clear separation between customer-visible communication
and agent-only notes.
---
7. Dashboard
Customer Dashboard
Customer statistics are calculated from the customer's own tickets.
The dashboard includes:
Total tickets.
Open tickets.
In-progress tickets.
Resolved tickets.
Closed tickets.
Urgent tickets.
Support Agent Dashboard
Agent statistics are calculated across the platform.
The dashboard includes:
Total tickets.
Open queue.
In-progress tickets.
Resolved tickets.
Urgent tickets.
Unassigned ticket queue.
Urgent ticket queue.
This gives agents a quick overview of the current support workload.
---
8. Search, Filtering and Pagination
The ticket API supports:
Search
Tickets can be searched using the ticket search parameter.
Filters
Tickets can be filtered by:
Status.
Priority.
Category.
Assigned agent.
Unassigned state.
Pagination
The backend uses Spring Data pagination with configurable:
Page number.
Page size.
Sort field.
Sort direction.
This prevents the application from needing to load every ticket at once.
---
9. Ticket Assignment
Support agents can assign a ticket to a support agent.
If an open ticket is assigned, the backend automatically moves it to:
``` text
IN_PROGRESS
```
The system also prevents customers from performing assignment
operations.
---
10. Technology Stack
Frontend
Technology                 Purpose
---
React 19                   User interface
TypeScript                 Type-safe frontend development
Vite                       Development server and build tool
React Router DOM           Client-side routing
Axios                      REST API communication
Tailwind CSS               Styling and responsive layouts
Lucide React               UI icons
clsx                       Conditional class handling
class-variance-authority   Reusable component variants
tailwind-merge             Tailwind class merging
Backend
Technology          Purpose
---
Java 21             Backend programming language
Spring Boot 3.3.4   Application framework
Spring Web          REST APIs
Spring Data JPA     Database persistence
Hibernate           ORM
Spring Security     Authentication and authorization
BCrypt              Password hashing
JWT / JJWT          Stateless authentication
Spring Validation   Request validation
Lombok              Boilerplate reduction
Maven               Build and dependency management
Database
Technology          Purpose
---
MySQL               Relational database
MySQL Connector/J   Java-to-MySQL connectivity
---
11. Project Structure
``` text
Customer support ticketing system/
│
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   │
│   └── src/
│       └── main/
│           ├── java/com/supportdesk/
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── entity/
│           │   ├── enums/
│           │   ├── exception/
│           │   ├── repository/
│           │   ├── security/
│           │   ├── service/
│           │   └── SupportDeskApplication.java
│           │
│           └── resources/
│               └── application.yml
│
└── frontend/
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    │
    └── src/
        ├── api/
        ├── assets/
        ├── components/
        │   ├── common/
        │   ├── layout/
        │   ├── tickets/
        │   └── ui/
        ├── context/
        ├── lib/
        ├── pages/
        │   ├── agent/
        │   ├── auth/
        │   ├── customer/
        │   └── shared/
        ├── routes/
        ├── types/
        ├── App.tsx
        ├── App.css
        ├── index.css
        └── main.tsx
```
---
12. Database Design
The application uses JPA entities to create and manage the relational
database structure.
Users
Stores account and role information.
Important fields include:
ID
Full name
Email
Password hash
Contact number
Agent ID
Role
Created/updated timestamps
Tickets
Stores support requests.
Important fields include:
ID
Ticket number
Title
Description
Status
Priority
Category
Customer
Assigned agent
Created timestamp
Updated timestamp
Resolved timestamp
Ticket Messages
Stores ticket conversations.
Important fields include:
ID
Ticket
Sender
Message
Internal-note flag
Created timestamp
---
13. REST API Overview
The backend exposes REST APIs under `/api`.
Authentication
``` text
POST /api/auth/register
POST /api/auth/register/customer
POST /api/auth/register/agent
POST /api/auth/login
GET  /api/auth/me
```
Tickets
``` text
GET   /api/tickets
POST  /api/tickets
GET   /api/tickets/{id}
PATCH /api/tickets/{id}/status
PATCH /api/tickets/{id}/priority
PATCH /api/tickets/{id}/assign
```
Ticket Messages
``` text
GET  /api/tickets/{ticketId}/messages
POST /api/tickets/{ticketId}/messages
```
Dashboard
``` text
GET /api/dashboard/stats
```
Users
``` text
GET /api/users/agents
```
---
14. Authentication Flow
``` text
User
  ↓
Login / Registration
  ↓
React
  ↓
Axios
  ↓
Spring Boot REST API
  ↓
Spring Security
  ↓
Database User Verification
  ↓
BCrypt Password Verification
  ↓
JWT Generation
  ↓
Frontend stores authentication state
  ↓
Protected API Requests
  ↓
JWT Authentication Filter
  ↓
Authorized Resource
```
The frontend attaches the JWT as:
``` text
Authorization: Bearer <token>
```
The backend validates the token before allowing access to protected
resources.
---
15. Security
The project includes several basic security controls.
Password hashing
Passwords are stored using BCrypt rather than plain text.
JWT authentication
The backend uses stateless JWT-based authentication.
Role-based authorization
Protected operations are restricted based on the authenticated user's
role.
Customer ticket isolation
Customers can only access tickets belonging to their own account.
Internal notes
Customer users cannot see support-agent internal notes.
CORS
The backend is configured to allow the frontend development origin.
Validation
Request DTOs use Jakarta Bean Validation and backend services also
enforce business rules.
---
16. Steps to Run the Project
Prerequisites
Install the following:
Java 21
Maven or use the included Maven Wrapper
Node.js and npm
MySQL Server
A code editor such as VS Code or IntelliJ IDEA
Check Java:
``` bash
java -version
```
Check Node.js:
``` bash
node -v
```
Check npm:
``` bash
npm -v
```
---
17. Database Setup
Start MySQL.
The application is configured to use:
``` text
Database: supportdesk_db
Host: localhost
Port: 3306
Username: root
```
The configured JDBC URL uses:
``` text
jdbc:mysql://localhost:3306/supportdesk_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```
The application uses:
``` text
spring.jpa.hibernate.ddl-auto=update
```
so Hibernate can create/update the required tables from the JPA
entities.
> **Important:** The database password in `application.yml` is currently
> stored as a local-development configuration value. For GitHub
> submission or production use, move database credentials and JWT
> secrets into environment variables or an external configuration
> mechanism. Never commit real credentials or secrets.
---
18. Start the Backend
Open a terminal inside:
``` text
backend/
```
Windows
Using Maven Wrapper:
``` bash
mvnw.cmd spring-boot:run
```
or, if Maven is installed:
``` bash
mvn spring-boot:run
```
The backend is configured to run on:
``` text
http://localhost:8080
```
---
19. Start the Frontend
Open another terminal inside:
``` text
frontend/
```
Install dependencies:
``` bash
npm install
```
Start Vite:
``` bash
npm run dev
```
The frontend runs on:
``` text
http://localhost:5173
```
The Vite development server proxies `/api` requests to:
``` text
http://localhost:8080
```
Therefore the complete local setup is:
``` text
Browser
  ↓
React + Vite
localhost:5173
  ↓
/api proxy
  ↓
Spring Boot
localhost:8080
  ↓
MySQL
localhost:3306
```
---
20. Demo / Initial Data
The backend contains a startup data initializer.
It creates a development administrator account and a customer demo
account if they do not already exist.
It also creates sample tickets when the ticket table is empty.
For security, use only development/demo credentials locally and change
any credentials before deploying the application publicly.
---
21. Application Workflow
Customer workflow
``` text
Register
   ↓
Login
   ↓
Customer Dashboard
   ↓
Create Ticket
   ↓
Select Category + Priority
   ↓
Submit Ticket
   ↓
View Ticket
   ↓
Conversation
   ↓
Reply
   ↓
Track Status
   ↓
Close after Resolution
```
Support Agent workflow
``` text
Agent Registration / Login
   ↓
Agent Dashboard
   ↓
View Ticket Statistics
   ↓
View Unassigned / Urgent Tickets
   ↓
Open Ticket
   ↓
Assign / Take Ownership
   ↓
In Progress
   ↓
Reply / Internal Note
   ↓
Update Priority / Status
   ↓
Resolve
   ↓
Close
```
---
22. Key Assumptions
The following assumptions were used for the MVP:
The application is intended as a lightweight support-ticketing
system rather than an enterprise-scale helpdesk.
Customers create their own accounts.
Customer registration creates a `ROLE_CUSTOMER` account.
Support-agent registration requires an authorized Agent ID.
Agent IDs currently accepted by the application are within the
configured development range of 123--127.
Each ticket belongs to one customer.
A ticket can have one assigned support agent.
Customers can only access their own tickets.
Support agents can manage organization-wide tickets.
Internal notes are visible to support-side users but not customers.
Ticket status is represented by Open, In Progress, Resolved, or
Closed.
A customer reply to a resolved/closed ticket reopens it as In
Progress.
MySQL is the persistent relational database.
The application is primarily intended for local assessment/demo use.
AI functionality is not part of the current implementation; it was
an optional enhancement in the assessment and was not required for
the core MVP.
---
23. Results / Output
The implemented application provides the core support-ticketing workflow
required for the assessment.
Authentication
Users can register and log in using JWT-based authentication.
Customer experience
Customers can:
Create tickets.
View their tickets.
Search/filter tickets.
Open ticket details.
Read conversations.
Reply to tickets.
Track ticket status.
Close resolved tickets.
Support-agent experience
Support agents can:
View global ticket statistics.
View all tickets.
Search/filter tickets.
View urgent tickets.
View unassigned tickets.
Assign tickets.
Update priority.
Update status.
Reply to customers.
Add internal notes.
Data persistence
User, ticket, and message information is stored in MySQL through Spring
Data JPA/Hibernate.
Responsive frontend
The frontend is organized into reusable React/TypeScript components and
responsive layouts using Tailwind CSS.
---
24. Challenges Faced
Frontend--backend integration
The React application and Spring Boot backend run as separate
applications, so API routing, CORS, authentication headers, and the Vite
development proxy need to remain consistent.
Authentication and authorization
The system needed to support different permissions for customers and
support agents while maintaining stateless JWT authentication.
Ticket-level authorization
It is not enough to hide buttons in the frontend. The backend also
checks whether a customer owns a ticket before returning ticket details
or allowing conversation access.
Internal vs customer-visible messages
Support-agent internal notes need to be stored while remaining hidden
from customers. The backend therefore retrieves customer conversations
without internal notes.
Ticket state management
Ticket actions have business rules. For example, assigning an open
ticket changes it to In Progress, and a customer reply to a
resolved/closed ticket reopens it as In Progress.
Search and filtering
Ticket retrieval supports multiple filters and pagination, which
required combining request parameters with Spring Data specifications.
Responsive UI
The dashboard, ticket tables, ticket details, forms, and conversation
components need to remain usable across different screen sizes.
---
25. Possible Future Improvements
The current application focuses on the assessment MVP. Future versions
could include:
AI-powered support
Add optional AI features such as:
Automatic ticket classification.
Priority suggestion.
Ticket summarization.
Suggested agent replies.
Duplicate-ticket detection.
File attachments
Allow customers to attach:
Screenshots.
Documents.
Error logs.
Email notifications
Notify customers and agents when:
A ticket is created.
A ticket is assigned.
A new reply is posted.
A ticket is resolved.
Real-time communication
Use WebSockets or Server-Sent Events for live ticket conversations and
notifications.
Advanced analytics
Add:
Average resolution time.
Ticket volume trends.
Agent workload.
Category distribution.
Priority distribution.
SLA monitoring.
Audit log
Create a dedicated ticket activity history for actions such as:
Assignment.
Status changes.
Priority changes.
Replies.
Resolution.
Production deployment
A production deployment could use:
``` text
React Frontend
      ↓
Cloud Hosting / CDN
      ↓
Spring Boot REST API
      ↓
Managed MySQL
```
Additional production improvements would include externalized secrets,
HTTPS, database migrations, monitoring, logging, rate limiting, and
automated CI/CD.
---
26. Testing and Verification
The project includes Spring Boot testing dependencies and Spring
Security testing support in the backend build configuration.
For assessment verification, the main workflows should be checked
manually:
Authentication
Customer registration.
Agent registration.
Login.
Invalid credentials.
Protected route access.
Ticket management
Create ticket.
View ticket.
Search ticket.
Filter ticket.
Assign ticket.
Change priority.
Change status.
Conversations
Customer reply.
Agent reply.
Internal agent note.
Customer visibility restrictions.
Authorization
Customer cannot view another customer's ticket.
Customer cannot assign tickets.
Customer cannot change priority.
Agent can manage tickets.
Frontend
Login.
Registration.
Dashboard.
Ticket list.
Create ticket.
Ticket details.
Conversation.
Responsive layouts.
---
27. Conclusion
SupportDesk converts a real-world customer support requirement into a
structured full-stack application.
The project demonstrates how a support request can move through the
complete lifecycle:
``` text
Customer
   ↓
Create Ticket
   ↓
Categorize + Prioritize
   ↓
Support Queue
   ↓
Agent Assignment
   ↓
Conversation
   ↓
In Progress
   ↓
Resolution
   ↓
Closure
```
From a technical perspective, the application combines React +
TypeScript + Vite on the frontend with Java 21 + Spring Boot +
Spring Security + JPA/Hibernate on the backend and MySQL for
persistent storage.
The implementation separates frontend components, REST controllers,
services, repositories, security, entities, and database
responsibilities. JWT authentication and BCrypt password hashing provide
the foundation for secure access, while role-based authorization and
ticket ownership checks protect ticket data.
The project also goes beyond a basic CRUD implementation by including
ticket assignment, pagination, filtering, internal notes, automatic
ticket state transitions, customer-specific dashboards, agent
operational queues, and a structured conversation system.
The result is a maintainable MVP that demonstrates the complete
engineering process expected from the assessment:
Requirement → Design → Development → Database → API → UI →
Authentication → Authorization → Testing → Documentation → Delivery.
---
Project Information
Project: SupportDesk --- Customer Support Ticketing System
Frontend: React + TypeScript + Vite
Backend: Java 21 + Spring Boot 3.3.4
Database: MySQL
Authentication: Spring Security + JWT
Password Security: BCrypt
ORM: Spring Data JPA / Hibernate
Styling: Tailwind CSS
API Communication: Axios + REST
Development Ports:
``` text
Frontend: http://localhost:5173
Backend:  http://localhost:8080
MySQL:    localhost:3306
```
Application Type: Full-Stack Customer Support Ticketing MVP
