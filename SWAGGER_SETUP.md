# OpenAPI/Swagger Integration Guide

## Overview
Your Student Management API now includes full OpenAPI 3.0 / Swagger UI documentation support using **Springdoc-OpenAPI**.

## What Was Added

### 1. **Dependencies (pom.xml)**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.4</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-common</artifactId>
    <version>2.0.4</version>
</dependency>
```

### 2. **Configuration Class**
- **File**: `src/main/java/com/students/crud/config/OpenApiConfig.java`
- Provides custom OpenAPI bean with API metadata
- Includes contact information and license details

### 3. **Application Properties**
Added configuration to `application.properties`:
```properties
# Springdoc-OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.api-docs.groups.enabled=true
```

### 4. **Enhanced StudentController**
Added comprehensive OpenAPI annotations:
- `@Tag` - API grouping and description
- `@Operation` - Endpoint operation details
- `@ApiResponses` - Response documentation
- `@ApiResponse` - Individual response codes and descriptions
- `@Content` - Response content media type
- `@Schema` - Data model documentation

## How to Access Swagger UI

### 1. **Start the Application**
```bash
cd /Users/karthik/IdeaProjects/Swagger
mvn spring-boot:run
```

Or run the packaged JAR:
```bash
java -jar target/crud-0.0.1-SNAPSHOT.jar
```

### 2. **Access the Swagger UI**
Open your browser and navigate to:
```
http://localhost:8081/swagger-ui.html
```

### 3. **OpenAPI JSON Specification**
Access the raw OpenAPI specification at:
```
http://localhost:8081/v3/api-docs
```

### 4. **Try the APIs**
In the Swagger UI:
- Click on any endpoint to expand it
- Click "Try it out" button
- Configure parameters (pagination, sorting)
- Click "Execute" to test the endpoint
- View the response

## API Endpoints Documented

### 1. **List All Students**
- **Method**: GET
- **Path**: `/api/students`
- **Description**: Retrieve a paginated list of all students
- **Parameters**: 
  - `page` (optional): Page number (default: 0)
  - `size` (optional): Page size (default: 20)
  - `sort` (optional): Sorting criteria (default: registrationNo)
- **Response**: 200 - Student list with pagination metadata

### 2. **List Students as DTOs**
- **Method**: GET
- **Path**: `/api/students/dto`
- **Description**: Retrieve paginated simplified student DTOs with caching (TTL: 300 seconds)
- **Parameters**: Same as above
- **Response**: 200 - Simplified student DTO list

## Features

✅ **Auto-generated API Documentation** - No manual OpenAPI spec needed  
✅ **Interactive Swagger UI** - Try APIs directly from browser  
✅ **OpenAPI 3.0 Compliance** - Industry-standard API specification  
✅ **Response Codes & Descriptions** - Full HTTP status documentation  
✅ **Data Model Documentation** - Schema definitions with examples  
✅ **Pagination Support** - Springdoc automatically documents Pageable parameters  
✅ **Caching Information** - TTL documented in API descriptions  

## Customization

### Add More Endpoints
Add OpenAPI annotations to your controllers:
```java
@GetMapping("/{id}")
@Operation(summary = "Get student by ID", description = "Retrieve a specific student")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Student found"),
    @ApiResponse(responseCode = "404", description = "Student not found")
})
public ResponseEntity<Student> getStudent(@PathVariable Long id) {
    // implementation
}
```

### Update API Metadata
Edit `OpenApiConfig.java` to change:
- API title and version
- Contact information
- License
- Server URLs
- API description

### Configure Swagger UI
Modify `application.properties`:
```properties
springdoc.swagger-ui.theme=dark  # Use dark theme
springdoc.swagger-ui.urls-primary-name=production  # Primary server
```

## Common Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/v3/api-docs` | GET | OpenAPI JSON specification |
| `/v3/api-docs.yaml` | GET | OpenAPI YAML specification |
| `/swagger-ui.html` | GET | Interactive Swagger UI |
| `/swagger-ui/` | GET | Alternative Swagger UI path |

## Troubleshooting

**Issue**: Swagger UI not loading  
**Solution**: Ensure `springdoc-openapi-starter-webmvc-ui` dependency is in pom.xml

**Issue**: Endpoints not appearing in Swagger  
**Solution**: Make sure controller is annotated with `@RestController` or `@Controller`

**Issue**: Pagination parameters not showing  
**Solution**: Add `springdoc-openapi-starter-common` dependency

## Next Steps

1. Document additional endpoints with `@Operation` annotations
2. Add `@RequestBody` and `@PathVariable` annotations for better parameter documentation
3. Include example request/response bodies using `@io.swagger.v3.oas.annotations.media.ExampleObject`
4. Add server configuration in `OpenApiConfig` for different environments

## Resources

- [Springdoc-OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

