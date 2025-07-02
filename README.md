# Identity Server

A comprehensive Identity Server built with Spring Boot for managing user authentication, authorization, and OAuth2 client management.

## 🏗️ Architecture

The project follows a clean, domain-driven architecture with clear separation of concerns:

```
com.codestorykh.alpha/
├── common/                    # Shared components
│   ├── domain/               # BaseEntity
│   ├── repository/           # BaseRepository
│   ├── service/              # BaseService
│   └── dto/                  # BaseDTO
├── identity/                 # User management
│   ├── domain/               # User, Role, Group, Permission
│   ├── repository/           # UserRepository, RoleRepository, etc.
│   ├── service/              # UserService, RoleService, etc.
│   ├── controller/           # UserController
│   └── dto/                  # UserDTO, RoleDTO, etc.
├── oauth2/                   # OAuth2 management
│   ├── domain/               # OAuth2Client, GrantType, TokenEndpointAuthMethod
│   ├── repository/           # OAuth2ClientRepository
│   ├── service/              # OAuth2ClientService
│   ├── controller/           # OAuth2ClientController
│   └── dto/                  # OAuth2ClientDTO
├── auth/                     # Authentication & Authorization
│   ├── service/              # Authentication services
│   └── controller/           # Auth controllers
├── admin/                    # Administrative functions
│   ├── service/              # HealthService
│   └── controller/           # HealthController
├── config/                   # Configuration classes
├── security/                 # Security components
├── exception/                # Exception handling
├── validation/               # Validation logic
├── utils/                    # Utility classes
└── constant/                 # Constants
```

## 🚀 Features

### Identity Management
- **User Management**: Create, update, delete, and manage users
- **Role-Based Access Control**: Assign roles and permissions to users
- **Group Management**: Organize users into groups with inherited permissions
- **Permission System**: Fine-grained permission control
- **Account Security**: Password policies, account locking, failed login attempts

### OAuth2/OpenID Connect
- **OAuth2 Client Management**: Register and manage OAuth2 clients
- **Multiple Grant Types**: Support for authorization code, client credentials, refresh token
- **Scope Management**: Define and validate OAuth2 scopes
- **Client Authentication**: Multiple authentication methods
- **Token Management**: Access and refresh token handling

### Security
- **JWT Authentication**: Stateless JWT-based authentication
- **Role-Based Authorization**: Method-level security with @PreAuthorize
- **CORS Configuration**: Cross-origin resource sharing support
- **Password Encryption**: BCrypt password hashing
- **Account Locking**: Automatic account locking after failed attempts

## 📋 API Endpoints

### User Management
- `GET /api/users` - Get all users (ADMIN, USER_MANAGER)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user (ADMIN)
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (ADMIN)
- `POST /api/users/{id}/enable` - Enable user (ADMIN)
- `POST /api/users/{id}/disable` - Disable user (ADMIN)
- `GET /api/users/search` - Search users
- `GET /api/users/by-role/{roleName}` - Get users by role (ADMIN)
- `GET /api/users/by-group/{groupName}` - Get users by group (ADMIN)

### OAuth2 Client Management
- `GET /api/oauth2/clients` - Get all OAuth2 clients (ADMIN)
- `GET /api/oauth2/clients/{id}` - Get client by ID (ADMIN)
- `GET /api/oauth2/clients/client-id/{clientId}` - Get client by client ID (ADMIN)
- `POST /api/oauth2/clients` - Create new OAuth2 client (ADMIN)
- `PUT /api/oauth2/clients/{clientId}` - Update OAuth2 client (ADMIN)
- `DELETE /api/oauth2/clients/{clientId}` - Delete OAuth2 client (ADMIN)
- `POST /api/oauth2/clients/{clientId}/enable` - Enable client (ADMIN)
- `POST /api/oauth2/clients/{clientId}/disable` - Disable client (ADMIN)
- `POST /api/oauth2/clients/{clientId}/regenerate-secret` - Regenerate client secret (ADMIN)

### Health Monitoring
- `GET /api/health` - System health status
- `GET /api/health/ping` - Simple ping endpoint

## 🔧 Configuration

### Database
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP
- **JPA**: Hibernate with automatic schema generation

### Security
- **Password Encoder**: BCrypt with strength 12
- **JWT**: Configurable JWT authentication
- **CORS**: Configurable cross-origin settings

### OAuth2
- **Authorization Server**: Spring Authorization Server
- **Resource Server**: JWT-based resource server
- **Client Storage**: Database-backed client storage

## 🛠️ Development

### Prerequisites
- Java 23
- Maven 3.8+
- PostgreSQL 12+

### Running the Application
1. Start PostgreSQL database
2. Configure database connection in `application.yml`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Building
```bash
mvn clean package
```

### Testing
```bash
mvn test
```

## 📝 Next Steps

1. **Service Implementations**: Implement the service interfaces
2. **OAuth2 Integration**: Complete OAuth2/OpenID Connect endpoints
3. **Testing**: Add comprehensive unit and integration tests
4. **Documentation**: Add OpenAPI/Swagger documentation
5. **Monitoring**: Add metrics and monitoring
6. **Deployment**: Docker containerization and deployment scripts

## 🔒 Security Considerations

- All sensitive endpoints require appropriate roles
- Password validation and encryption
- Account locking mechanisms
- JWT token security
- CORS configuration
- Input validation and sanitization

## 📄 License

This project is licensed under the MIT License. 
