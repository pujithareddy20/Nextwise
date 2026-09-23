# SupportDesk: Technical Interview Preparation & Architecture Guide

A comprehensive architectural and engineering guide for explaining the **SupportDesk — Customer Support Ticketing System** during technical interviews, system design rounds, and code reviews.

---

## 1. System Architecture & High-Level Design

### 1.1 Architecture Diagram
```
+------------------------------------------------------------------+
|                     Client Tier (Browser)                        |
|   React 19 + TypeScript + Vite + Tailwind CSS + Lucide Icons    |
+---------------------------------+--------------------------------+
                                  | HTTP / REST (JSON)
                                  | Authorization: Bearer <JWT>
                                  v
+------------------------------------------------------------------+
|              Spring Boot 3.3.4 (Java 21 LTS) Engine              |
|                                                                  |
|  [ Security Filter Chain ]                                       |
|    - CorsFilter -> JwtAuthenticationFilter -> UsernamePassword   |
|    - SecurityContextHolder (UserPrincipal)                       |
|                                                                  |
|  [ Controller Layer ]                                            |
|    - AuthController, TicketController, TicketMessageController,  |
|      DashboardController, UserController                         |
|    - Jakarta Validation (@Valid, @NotBlank, @Size)               |
|                                                                  |
|  [ Service Layer (Business Logic & OWASP BOLA Defense) ]         |
|    - AuthService, TicketService, TicketMessageService,           |
|      DashboardService, UserService                               |
|    - @Transactional boundaries                                   |
|                                                                  |
|  [ Data Access Layer (Spring Data JPA / Hibernate 6) ]          |
|    - JPA Specifications (Dynamic CriteriaBuilder filtering)      |
|    - @EntityGraph (N+1 Query Elimination via JOIN FETCH)         |
|                                                                  |
+---------------------------------+--------------------------------+
                                  | JDBC / HikariCP Pool
                                  v
+------------------------------------------------------------------+
|                Database Tier (MySQL 8.0 / InnoDB)                |
|   Tables: users, tickets, ticket_messages                        |
|   Indexes: status, priority, customer_id, assigned_to_id         |
+------------------------------------------------------------------+
```

### 1.2 Design Philosophy & Trade-offs
* **Modular Monolith over Microservices**: For a customer ticketing platform of this scope, a microservices architecture would introduce distributed transaction overhead, network latency, and unnecessary DevOps complexity. A clean, layered modular monolith with strict domain boundaries provides high throughput, simple debugging, and straightforward transactional guarantees (`@Transactional`).
* **Stateless RESTful Design**: No HTTP sessions are stored on the server (`SessionCreationPolicy.STATELESS`). Authentication state is encapsulated inside digitally signed JSON Web Tokens (JWT). This enables seamless horizontal scaling across multiple application instances behind a load balancer without sticky sessions.
* **Strict Separation of API DTOs and Database Entities**: Entities (`User`, `Ticket`, `TicketMessage`) are never exposed directly to controllers. All inputs and outputs use decoupled Data Transfer Objects (DTOs). This completely eliminates **Mass Assignment vulnerabilities**, protects database schema privacy, and prevents circular reference serialization errors.

---

## 2. Security Architecture & Spring Security 6 Deep Dive

### 2.1 Spring Security 6 Component Model
Spring Boot 3.x uses Spring Security 6, which completely removed the deprecated `WebSecurityConfigurerAdapter` in favor of component-based bean registration:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tickets/**").authenticated()
                .requestMatchers("/api/tickets/**").authenticated()
                .requestMatchers("/api/dashboard/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 2.2 JWT Authentication Flow
1. **Client Request**: Client sends credentials (`email`, `password`) to `/api/auth/login`.
2. **AuthenticationManager & ProviderManager**: Authenticates credentials using `DaoAuthenticationProvider` and `BCryptPasswordEncoder`.
3. **Token Issuance**: `JwtTokenProvider` signs an HMAC-SHA token (using `jjwt` 0.12.x) containing claims:
   - `sub`: User email
   - `id`: User primary key
   - `role`: User authority (`ROLE_CUSTOMER`, `ROLE_AGENT`, `ROLE_ADMIN`)
   - `fullName`: Display name
   - `exp`: Expiration timestamp (24 hours)
4. **Subsequent Invocations**: Every request contains header:
   `Authorization: Bearer <token>`
5. **Filter Interception**: `JwtAuthenticationFilter` executes once per request:
   - Extracts and strips the `Bearer ` prefix.
   - Validates the cryptographic signature against the secret key.
   - Extracts the username (`email`) and loads the `UserPrincipal`.
   - Creates a `UsernamePasswordAuthenticationToken` and installs it in `SecurityContextHolder.getContext()`.

### 2.3 Dependency Injection in Custom Filters
> **Interview Insight**: In Spring Boot, when a filter extends `OncePerRequestFilter`, fields declared as `private final` must be injected via a concrete constructor:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }
    ...
}
```
*Why this matters*: Relying on Lombok's `@RequiredArgsConstructor` can fail in certain IDE annotation processing pipelines or compiler configurations under Java 21, resulting in `The blank final field may not have been initialized`. Explicit constructors eliminate compiler ambiguity and adhere to standard inversion of control.

---

## 3. OWASP Top 10 Defenses in SupportDesk

| OWASP Vulnerability | Threat Description | SupportDesk Defense Implementation |
|---|---|---|
| **BOLA / IDOR** (Broken Object Level Authorization) | Attacker changes `ticketId` in URL to view or modify other users' tickets | In `TicketServiceImpl.getTicketById` and `TicketMessageServiceImpl.addMessage`, ownership is strictly verified: `if (currentUser.getRole() == Role.ROLE_CUSTOMER && !ticket.getCustomer().getId().equals(currentUser.getId())) throw new AccessDeniedException(...)` |
| **Mass Assignment** | Malicious client submits privileged fields (e.g. `role: "ROLE_ADMIN"` or `assignedTo`) | Strictly defined Request DTOs (`RegisterRequest`, `CreateTicketRequest`). Role is defaulted server-side to `ROLE_CUSTOMER`. |
| **Broken Function Level Auth** | Customer tries to invoke agent endpoints (assign, update status) | Method-level security: `@PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_ADMIN')")` on assignment and status endpoints. |
| **Information Disclosure** | Internal team notes leaked to customers | In `TicketMessageServiceImpl.getMessagesByTicketId`, if caller is `ROLE_CUSTOMER`, internal notes are filtered out at the database level: `findByTicketIdAndIsInternalNoteFalseOrderByCreatedAtAsc`. |
| **SQL Injection** | Attacker injects SQL fragments into search filters | Spring Data JPA with parameterized queries and Hibernate CriteriaBuilder API (`TicketSpecification`). No string concatenation in queries. |
| **XSS** (Cross-Site Scripting) | Stored malicious scripts in ticket descriptions or messages | React automatic JSX expression escaping + Jakarta Bean Validation (`@NotBlank`, `@Size`, character validation). |
| **Weak Password Storage** | Compromised database leaks plaintext passwords | Spring Security `BCryptPasswordEncoder` with strong salt rounds. Plaintext passwords never stored or logged. |

---

## 4. Database Architecture & JPA Performance Tuning

### 4.1 Relational Schema Design
```
+--------------------+           +----------------------+
|       users        |           |       tickets        |
+--------------------+           +----------------------+
| id (PK, BigInt)    |<----+     | id (PK, BigInt)      |
| email (Unique)     |     +-----| customer_id (FK)     |
| full_name          |     +-----| assigned_to_id (FK)  |
| password (BCrypt)  |<----+     | ticket_number (Uniq) |
| role (Enum String) |           | title                |
| created_at         |           | description (Text)   |
| updated_at         |           | status (Enum String) |
+--------------------+           | priority (Enum)      |
                                 | category (Enum)      |
                                 | created_at           |
                                 | updated_at           |
                                 | resolved_at          |
                                 +----------+-----------+
                                            | 1
                                            | 
                                            | N
                                 +----------v-----------+
                                 |   ticket_messages    |
                                 +----------------------+
                                 | id (PK, BigInt)      |
                                 | ticket_id (FK)       |
                                 | sender_id (FK)       |
                                 | message (Text)       |
                                 | is_internal_note     |
                                 | created_at           |
                                 +----------------------+
```

### 4.2 Solving the N+1 Query Problem
When loading a `Ticket`, accessing `ticket.getCustomer()` and `ticket.getAssignedTo()` can trigger 2 additional queries per ticket row. For a list of 50 tickets, this results in **101 SQL queries** ($1 + 2 \times N$).

**SupportDesk Solution**:
We declare `@EntityGraph` on repository query methods:
```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @EntityGraph(attributePaths = {"customer", "assignedTo"})
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findWithDetailsById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"customer", "assignedTo"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);
}
```
*Generated SQL*: Hibernate executes a single query with SQL `LEFT OUTER JOIN` clauses, reducing database round-trips from $1+N$ to **exactly 1 query**.

### 4.3 Dynamic Filtering via JPA Criteria API (`Specification`)
Instead of writing multiple hardcoded JPQL queries for every combination of filters, SupportDesk uses Spring Data's `Specification<Ticket>`:
```java
public static Specification<Ticket> filterTickets(
        Long customerId,
        String search,
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        Long assignedAgentId,
        Boolean unassigned) {

    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();

        if (customerId != null) {
            predicates.add(cb.equal(root.get("customer").get("id"), customerId));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase().trim() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("ticketNumber")), pattern)
            ));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
```

---

## 5. Frontend Architecture & React 19 Best Practices

### 5.1 Clean Layered Architecture
* **API Client Layer (`src/api/`)**: `axiosClient` configured with `baseURL`, timeout, and request interceptor injecting JWT.
* **Context / State Layer (`src/context/`)**: `AuthContext` provides `user`, `token`, `login()`, `logout()`, `register()`, `isCustomer`, `isAgent`, `isAdmin`.
* **Component Layer (`src/components/`)**:
  - Primitives: `Button`, `Badge`, `Card`, `Input`, `Select`, `Textarea`.
  - Domain Components: `TicketStatusBadge`, `TicketPriorityBadge`, `ConversationThread`, `ReplyBox`, `TicketFilterBar`.
* **Page Layer (`src/pages/`)**: Separate views for customer workflow (`CustomerDashboardPage`, `CreateTicketPage`) and agent workflow (`AgentDashboardPage`, `AllTicketsPage`, `TicketDetailsPage`).
* **Route Guarding (`src/routes/`)**: `ProtectedRoute` checks authentication and role permissions, redirecting unauthorized users to `/unauthorized`.

### 5.2 Dynamic Metrics Engine
The dashboard does **not** rely on hardcoded dummy metrics. All KPIs are dynamically computed by the Spring Boot backend via JPA aggregate count queries (`countByCustomer`, `countByStatus`, `countByPriority`) and updated on the UI whenever tickets transition through their lifecycle.

---

## 6. Top 20 Technical Interview Questions & Model Answers

### Q1: What is the architectural difference between authentication and authorization in Spring Security?
**Answer**: 
- **Authentication** is the process of verifying *who* the principal is. In SupportDesk, `DaoAuthenticationProvider` validates the user's email and password, and `JwtAuthenticationFilter` validates incoming Bearer tokens and sets the `Authentication` token in `SecurityContextHolder`.
- **Authorization** is the process of verifying *what permissions* the authenticated principal has. In SupportDesk, authorization is enforced via URL request matchers in `SecurityFilterChain` (e.g., `/api/tickets/**`) and method-level security (`@PreAuthorize("hasRole('AGENT')")`).

---

### Q2: What is Broken Object Level Authorization (BOLA / IDOR) and how did you prevent it?
**Answer**: 
BOLA (Insecure Direct Object Reference) occurs when an application receives an object identifier from a client (e.g. `GET /api/tickets/5`) and accesses the resource without verifying whether the requesting user owns or has legitimate access to that object.
In SupportDesk, when a customer accesses ticket details or posts a message, we fetch the ticket and verify:
```java
if (currentUser.getRole() == Role.ROLE_CUSTOMER && !ticket.getCustomer().getId().equals(currentUser.getId())) {
    throw new AccessDeniedException("You do not have permission to access this ticket");
}
```
If the IDs do not match, Spring Security immediately throws `AccessDeniedException`, which our `GlobalExceptionHandler` converts to an HTTP 403 Forbidden response.

---

### Q3: Why did you use Spring Security 6's `ProviderManager` directly instead of exposing `DaoAuthenticationProvider` as a standalone Spring bean?
**Answer**: 
In Spring Boot 3 / Spring Security 6, exposing a `DaoAuthenticationProvider` bean while also having a `UserDetailsService` bean can trigger an `InitializeUserDetailsManagerConfigurer` warning because Spring Security attempts to auto-configure an in-memory or default authentication manager with circular references.
By instantiating `ProviderManager(authProvider)` inside the `@Bean AuthenticationManager` definition:
```java
@Bean
public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authProvider);
}
```
we explicitly control the provider hierarchy, avoid bean registration ambiguity, and ensure clean startup.

---

### Q4: How does Hibernate solve the N+1 query problem, and what did you implement?
**Answer**: 
The N+1 problem occurs when Hibernate executes 1 query for the parent entity (`Ticket`) and then executes $N$ additional queries to fetch lazily loaded child associations (`Customer` and `AssignedAgent`) for each returned row.
We solved this by applying `@EntityGraph(attributePaths = {"customer", "assignedTo"})` on repository query methods. Spring Data JPA instructs Hibernate to generate an explicit SQL `LEFT OUTER JOIN` in the initial query, loading the ticket and its associated users in a single database round-trip.

---

### Q5: How do you handle database migrations and schema consistency in production vs testing?
**Answer**: 
- In **Testing**, we use `application.yml` with in-memory H2 database (`MODE=MySQL`) and `spring.jpa.hibernate.ddl-auto=create-drop` to provide fast, isolated, hermetic tests that leave zero persistent artifacts.
- In **Development/Production**, we use local MySQL 8.0 with `ddl-auto=update` (or Flyway/Liquibase migration scripts in enterprise deployments) to preserve customer tickets and audit logs across server restarts.

---

### Q6: Why did you use constructor-based dependency injection in `JwtAuthenticationFilter`?
**Answer**: 
`JwtAuthenticationFilter` extends `OncePerRequestFilter`. If fields are marked `private final`, relying on Lombok `@RequiredArgsConstructor` can sometimes fail depending on compiler annotation processor sequencing, producing `The blank final field may not have been initialized`.
Writing an explicit constructor:
1. Guarantees immutable dependencies.
2. Ensures compatibility with standard Spring dependency injection.
3. Makes unit testing trivial by allowing easy mock injection without reflection.

---

### Q7: How are internal notes kept confidential from customers?
**Answer**: 
The database stores a boolean column `is_internal_note` on `ticket_messages`.
1. **Writing**: If a customer calls `POST /api/tickets/{id}/messages` with `internalNote: true`, the service overrides this server-side:
   ```java
   boolean internalNote = !isCustomer && request.isInternalNote();
   ```
   Customers are physically unable to create internal notes.
2. **Reading**: When retrieving the conversation:
   - For `ROLE_CUSTOMER`: The repository executes `findByTicketIdAndIsInternalNoteFalseOrderByCreatedAtAsc(ticketId)`. Internal notes are filtered at the SQL query level, never reaching the application layer.
   - For `ROLE_AGENT`: The repository executes `findByTicketIdOrderByCreatedAtAsc(ticketId)`, returning all public messages and internal notes.

---

### Q8: What is the purpose of `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`?
**Answer**: 
In Spring Boot 3.3+, returning a Spring Data `Page<T>` directly from a controller triggers a warning that Spring's default `PageImpl` JSON structure will change in future versions.
Adding `@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)` configures Spring to serialize pages using a standardized, future-proof DTO format with clean `content`, `pageNumber`, `pageSize`, `totalElements`, and `totalPages` keys.

---

### Q9: How do you handle Cross-Origin Resource Sharing (CORS) securely?
**Answer**: 
Instead of using unsafe wildcard `@CrossOrigin("*")` on controllers, SupportDesk configures a centralized `CorsConfigurationSource` in `SecurityConfig`:
```java
CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOrigins(List.of("http://localhost:5173"));
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
configuration.setAllowCredentials(true);
```
This ensures preflight `OPTIONS` requests are handled cleanly, prevents CSRF vector access from untrusted domains, and allows credentials/tokens to flow safely.

---

### Q10: How does the dynamic ticket filtering work without vulnerable SQL string concatenation?
**Answer**: 
SupportDesk uses the JPA `Specification<Ticket>` interface based on the Criteria API. A `Specification` defines a reusable predicate `(root, query, criteriaBuilder) -> Predicate`.
Each filter condition (`status`, `priority`, `category`, `search`) adds a strongly typed `Predicate` to a list. Hibernate transforms these predicates into parameterized SQL queries (`?`), preventing SQL injection attacks while supporting arbitrary combinations of filters.

---

### Q11: How do you prevent Mass Assignment in ticket creation?
**Answer**: 
If the controller accepted the `Ticket` entity directly, a malicious client could send JSON with `status: "RESOLVED"`, `ticketNumber: "FAKE-123"`, or `assignedTo: 1`.
By accepting a strict `CreateTicketRequest` containing only `title`, `description`, `category`, and `priority`, the service enforces business defaults:
- `ticketNumber` is generated via a cryptographically random UUID generator.
- `status` is hardcoded to `TicketStatus.OPEN`.
- `customer` is bound strictly to `currentUser` from the verified JWT.
- `assignedTo` is left `null` awaiting triage.

---

### Q12: How are transactions managed, and what happens if a message fails to save?
**Answer**: 
Service methods are annotated with `@Transactional`. For read queries, `@Transactional(readOnly = true)` optimizes database performance by instructing Hibernate to disable dirty-checking.
For mutation methods (like `addMessage`), if an exception is thrown after creating a message but before saving, the Spring transaction manager triggers an automatic rollback, preventing orphan or corrupted records.

---

### Q13: What happens when a customer replies to a resolved ticket?
**Answer**: 
In `TicketMessageServiceImpl.addMessage`, we implemented an automated lifecycle rule:
```java
if (isCustomer && (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED)) {
    ticket.setStatus(TicketStatus.IN_PROGRESS);
    ticket.setResolvedAt(null);
    ticketRepository.save(ticket);
}
```
If a customer indicates their issue is still occurring, the ticket automatically re-opens and transitions back to `IN_PROGRESS`, clearing the resolution timestamp and notifying agents on their dashboard.

---

### Q14: How does the frontend handle token expiration or unauthorized responses?
**Answer**: 
In `frontend/src/api/axiosClient.ts`, an Axios response interceptor intercepts all HTTP responses:
```typescript
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('supportdesk_token');
      localStorage.removeItem('supportdesk_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```
If the JWT expires, the client clears the corrupted session and redirects cleanly to `/login` with an intuitive notice.

---

### Q15: How are validation errors communicated back to the client?
**Answer**: 
DTO fields use Jakarta validation annotations (`@NotBlank`, `@Size`, `@Email`, `@NotNull`).
When a controller method receives invalid data, Spring throws `MethodArgumentNotValidException`.
Our `GlobalExceptionHandler` catches this and compiles a dictionary of field errors:
```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "validationErrors": {
    "title": "Title must be between 5 and 200 characters",
    "category": "Ticket category is required"
  }
}
```
The React frontend reads `validationErrors` to highlight the exact input fields in red.

---

### Q16: Why did you use Lucide React and Tailwind CSS instead of heavyweight UI libraries?
**Answer**: 
Component libraries like Material UI or Ant Design add substantial bundle size (often 500KB+), introduce opinionated CSS styling overrides, and make custom branding difficult.
Tailwind CSS with custom reusable primitives (similar to shadcn/ui) compiles down to atomic CSS with zero runtime JavaScript overhead, resulting in sub-second page loads and complete design flexibility.

---

### Q17: How did you test the backend to guarantee reliability?
**Answer**: 
We developed a two-tiered testing suite with 100% pass rate:
1. **Unit Tests (`TicketServiceTest`, `AuthServiceTest`)**: Mocking repositories with Mockito to test business rules, validation, and error states in isolation.
2. **Integration Tests (`@SpringBootTest`, `@AutoConfigureMockMvc`)**: Testing the full Spring Boot application context with MockMvc against an in-memory H2 database. Tests cover registration, login, JWT validation, customer scoping, agent assignment, BOLA defense, and conversation thread privacy.

---

### Q18: What indexes would you add to the MySQL database for scale?
**Answer**: 
For high ticket volumes (1M+ records):
1. `CREATE INDEX idx_tickets_customer ON tickets(customer_id);` — accelerates customer dashboard queries.
2. `CREATE INDEX idx_tickets_status ON tickets(status);` — accelerates agent queue filtering.
3. `CREATE INDEX idx_tickets_assigned ON tickets(assigned_to_id);` — accelerates "My Assigned Tickets".
4. `CREATE INDEX idx_messages_ticket_internal ON ticket_messages(ticket_id, is_internal_note);` — composite index for fast conversation retrieval while filtering notes.

---

### Q19: How would you scale SupportDesk to handle 100,000 requests per minute?
**Answer**: 
1. **Read-Write Database Splitting**: Use MySQL primary for writes and read replicas for dashboard aggregations and ticket queries.
2. **Redis Caching**: Cache user sessions, agent lists, and dashboard metric counts with short TTL (e.g. 30 seconds).
3. **Stateless Horizontal Scaling**: Run multiple Spring Boot container instances behind an NGINX or AWS ALB load balancer.
4. **Asynchronous Messaging**: Use RabbitMQ or Apache Kafka to handle ticket event notifications (e.g. sending customer confirmation emails or Slack alerts) outside the HTTP request-response cycle.

---

### Q20: If you had another sprint, what features would you add?
**Answer**: 
1. **File Attachments (AWS S3 / MinIO)**: Allowing customers to attach screenshots and logs to tickets with presigned upload URLs.
2. **WebSocket / SSE Live Updates**: Real-time push notifications so agents see new customer replies instantly without manual refreshing.
3. **SLA Management**: Automated timer tracking response times with alerts for tickets nearing breach.
4. **Full-Text Search (Elasticsearch)**: For fast semantic search across millions of historical ticket threads.
