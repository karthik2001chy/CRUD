# Spring Bean Lifecycle - Complete Guide with CRUD Application Examples

## Overview
The Spring Bean Lifecycle is the process that manages beans from creation to destruction. Understanding this is crucial for writing efficient Spring applications.

---

## Phase 1: Component Scanning & Bean Detection

### What Happens?
Spring scans the classpath for classes annotated with:
- **@Component** - Generic component
- **@Service** - Business logic layer
- **@Repository** - Data access layer
- **@Controller** - Web layer
- **@Configuration** - Configuration class

### In Your CRUD Application:

```java
// CrudApplication.java
@SpringBootApplication  // This enables @ComponentScan automatically
@EnableCaching
public class CrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);
    }
}
```

**What Spring does:**
1. Scans `com.students.crud` package and sub-packages
2. Finds all annotated classes
3. Collects metadata about them
4. Doesn't create instances yet (only builds a registry)

### Beans Found in Your App:

```
✓ StudentController        @RestController (extends @Component)
✓ StudentService           @Service
✓ StudentRepository        @Repository
✓ StudentMapper            @Component
✓ RedisConfig              @Configuration
✓ OpenApiConfig            @Configuration
```

**Execution Order for Detection:**
```
1. CrudApplication class loaded
2. @ComponentScan triggered (automatic in @SpringBootApplication)
3. Classpath scanned for @Component, @Service, @Repository, @Controller
4. BeanDefinition created for each found class
```

---

## Phase 2: Bean Instantiation

### What Happens?
Spring creates instances of beans. The order depends on:
- Dependencies between beans
- Explicit @Order annotation
- Natural discovery order

### In Your CRUD Application:

**Step 1: StudentRepository is instantiated**
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // Spring creates a dynamic proxy implementation
    // No explicit constructor needed - interface!
}

// Spring internally creates something like:
// StudentRepository proxy = new StudentRepositoryImpl();
```

**Why first?** It has NO dependencies.

---

**Step 2: StudentMapper is instantiated**
```java
@Component
public class StudentMapper {
    // No dependencies on other beans
    // Pure utility class with stateless methods
    
    public StudentSimpleDto toSimpleDto(Student s) {
        if (s == null) return null;
        return new StudentSimpleDto(s.getRegistrationNo(), s.getName(), ...);
    }
}

// Spring does: StudentMapper mapper = new StudentMapper();
```

---

**Step 3: StudentService is instantiated**
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    // Constructor Injection - dependencies are resolved here
    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;  // Injected from container
        this.studentMapper = studentMapper;           // Injected from container
    }
    
    // ... methods
}

// Spring does:
// StudentService service = new StudentService(
//     container.getBean(StudentRepository.class),
//     container.getBean(StudentMapper.class)
// );
```

---

**Step 4: StudentController is instantiated**
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;  // Injected
    }
    
    // REST endpoints...
}

// Spring does:
// StudentController controller = new StudentController(
//     container.getBean(StudentService.class)
// );
```

---

**Step 5: Configuration Beans**
```java
@Configuration
public class RedisConfig {
    @Value("${app.cache.student-simple.ttl:300}")
    private long studentSimpleTtlSeconds;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Spring calls this method and registers the returned object as a bean
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheConfiguration studentSimpleConfig = defaultConfig
                .entryTtl(Duration.ofSeconds(studentSimpleTtlSeconds));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("studentSimple", studentSimpleConfig)
                .build();
    }
}

// Spring does:
// RedisCacheManager bean = redisConfig.cacheManager(
//     container.getBean(RedisConnectionFactory.class)
// );
```

---

### Instantiation Order Summary:
```
┌─────────────────────────────────────────────┐
│ INSTANTIATION PHASE (Order Matters!)        │
├─────────────────────────────────────────────┤
│ 1. StudentRepository (no deps)              │
│ 2. StudentMapper (no deps)                  │
│ 3. StudentService (deps: 1, 2)              │
│ 4. StudentController (deps: 3)              │
│ 5. RedisConfig, OpenApiConfig               │
│ 6. RedisConnectionFactory, RedisCacheManager│
└─────────────────────────────────────────────┘
```

---

## Phase 3: Dependency Injection

### How @Autowired Works (Implicit via Constructor)

In your CRUD app, you use **Constructor Injection** (Best Practice):

```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    // Spring automatically injects dependencies here
    // No @Autowired needed - Spring is smart enough!
    public StudentService(
        StudentRepository studentRepository,
        StudentMapper studentMapper
    ) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }
}
```

### How Spring Resolves Dependencies:

```
When Spring creates StudentService:
1. It looks at the constructor parameters
2. Checks: "Is there a StudentRepository bean?"
3. Checks: "Is there a StudentMapper bean?"
4. Gets both from the container
5. Calls: new StudentService(repo, mapper)
6. Injects them into the service

Result: studentRepository and studentMapper are now available
```

### Alternative: @Autowired (Field Injection - Less Preferred)

```java
@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private StudentMapper studentMapper;
    
    // Fields are injected after constructor
}
```

**Problem:** Breaks immutability. Constructor injection is better.

---

## Phase 4: Aware Interfaces

These interfaces let beans know about their container context.

### BeanNameAware
Bean learns its name in the container.

```java
@Component
public class StudentMapperWithAwareness 
        implements BeanNameAware {
    
    private String beanName;
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("My bean name is: " + name);
        // Output: My bean name is: studentMapperWithAwareness
    }
}
```

### ApplicationContextAware
Bean gets access to the Spring container itself.

```java
@Component
public class StudentServiceWithContext 
        implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) 
            throws BeansException {
        this.applicationContext = context;
        System.out.println("I now have access to ApplicationContext!");
        
        // Example: Get any bean at runtime
        StudentRepository repo = context.getBean(StudentRepository.class);
    }
}
```

### When Are Aware Methods Called?

```
1. Constructor called
2. Dependencies injected
3. ↓ Aware Methods Called ↓
4. setBeanName() - if implements BeanNameAware
5. setApplicationContext() - if implements ApplicationContextAware
6. ↓
7. @PostConstruct methods
```

---

## Phase 5: BeanPostProcessor

These process beans BEFORE and AFTER initialization.

### Common Use Cases:
- Logging bean creation
- Modifying bean properties
- Proxying beans (used by @Transactional, @Cacheable)

### Custom BeanPostProcessor in Your App:

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        System.out.println("BEFORE Init: " + beanName);
        
        // Called before @PostConstruct and InitializingBean.afterPropertiesSet()
        if (beanName.equals("studentService")) {
            System.out.println("StudentService bean is being initialized!");
        }
        
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        System.out.println("AFTER Init: " + beanName);
        
        // Called after @PostConstruct and InitializingBean.afterPropertiesSet()
        // Spring proxies @Transactional, @Cacheable, @Async here
        
        return bean;
    }
}
```

### What Spring Caching Does (BeanPostProcessor in Action):

```java
@Service
public class StudentService {
    
    // Spring sees @Cacheable annotation
    // BeanPostProcessor wraps this method in caching logic
    @Cacheable(value = "studentSimpleList")
    public List<StudentSimpleDto> getAllSimple() {
        System.out.println("This might not run if cached!");
        return studentMapper.toSimpleDtoList(studentRepository.findAll());
    }
}

// Spring internally does something like:
class CachingProxy extends StudentService {
    @Override
    public List<StudentSimpleDto> getAllSimple() {
        // Check cache first
        if (cache.contains("studentSimpleList")) {
            return cache.get("studentSimpleList");
        }
        
        // If not cached, call original
        List<StudentSimpleDto> result = super.getAllSimple();
        
        // Store in cache
        cache.put("studentSimpleList", result);
        
        return result;
    }
}
```

---

## Phase 6: Initialization Phase

### What Happens?
After instantiation and dependency injection, Spring initializes the bean.

### Three Ways to Initialize:

#### 1. @PostConstruct (Preferred)

```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    
    // List to cache loaded students (just example)
    private List<Student> cachedStudents;
    
    public StudentService(StudentRepository studentRepository, 
                         StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        System.out.println("1. Constructor called");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("3. @PostConstruct called - initialization logic");
        
        // Load all students into memory (optimization)
        this.cachedStudents = studentRepository.findAll();
        
        System.out.println("Loaded " + cachedStudents.size() + " students");
    }
    
    public Page<Student> getStudents(Pageable pageable) {
        System.out.println("6. getStudents called during API request");
        return studentRepository.findAll(pageable);
    }
}
```

**Output:**
```
1. Constructor called
2. Dependency injection happens (silent)
3. @PostConstruct called - initialization logic
Loaded 0 students (or N students if DB has data)
```

#### 2. InitializingBean Interface

```java
@Component
public class StudentMapperWithInitializing 
        implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("3. InitializingBean.afterPropertiesSet() called");
        System.out.println("All dependencies are injected now!");
    }
}
```

#### 3. init-method in XML (Legacy)

```xml
<!-- applicationContext.xml -->
<bean id="studentService" 
      class="com.students.crud.Services.StudentService"
      init-method="initialize">
</bean>
```

```java
@Service
public class StudentService {
    public void initialize() {
        // Called during initialization
    }
}
```

### Execution Order for Initialization:

```
1. Constructor called
2. Setter injection / Dependency injection
3. Aware interfaces (setBeanName, setApplicationContext, etc.)
4. BeanPostProcessor.postProcessBeforeInitialization()
5. ↓ INITIALIZATION HAPPENS ↓
6. @PostConstruct methods
7. InitializingBean.afterPropertiesSet()
8. init-method (XML)
9. BeanPostProcessor.postProcessAfterInitialization()
10. Bean is ready to use!
```

---

## Phase 7: Bean Usage During API Request

### Real Example: GET /api/students

```
┌────────────────────────────────────────────────────┐
│ CLIENT: GET /api/students?page=0&size=20           │
└────────────────────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────┐
│ Spring DispatcherServlet receives request          │
└────────────────────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────┐
│ Maps to: StudentController.listStudents()          │
│ Gets StudentController bean from container         │
└────────────────────────────────────────────────────┘
                         ↓
```

**Controller Code:**
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;  // Already created & injected

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List all students (paginated)")
    public ResponseEntity<Page<Student>> listStudents(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {
        
        System.out.println(">>> STEP 1: Controller.listStudents() called");
        
        Page<Student> page = studentService.getStudents(pageable);
        return ResponseEntity.ok(page);
    }
}
```

                         ↓
```
┌────────────────────────────────────────────────────┐
│ Calls: StudentService.getStudents(pageable)        │
│ Service bean already exists in container           │
└────────────────────────────────────────────────────┘
                         ↓
```

**Service Code:**
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, 
                         StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    @Cacheable(value = "studentSimple", key = "#pageable.pageNumber...")
    public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
        System.out.println(">>> STEP 2: Service.getStudents() called");
        
        // Check cache first (BeanPostProcessor did this)
        // If hit: return cached result immediately
        // If miss: continue...
        
        Page<Student> studentPage = studentRepository.findAll(pageable);
        return studentPage.map(studentMapper::toSimpleDto);
    }

    public Page<Student> getStudents(Pageable pageable) {
        System.out.println(">>> STEP 2: Service.getStudents() called");
        return studentRepository.findAll(pageable);
    }
}
```

                         ↓
```
┌────────────────────────────────────────────────────┐
│ Calls: StudentRepository.findAll(pageable)         │
│ Repository proxy bean delegates to DB              │
└────────────────────────────────────────────────────┘
                         ↓
```

**Repository Code:**
```java
@Repository
public interface StudentRepository 
        extends JpaRepository<Student, String> {
    // Spring provides implementation via proxy
    // JpaRepository.findAll(Pageable) executes SQL
}

// Spring internally generates:
// SELECT * FROM students LIMIT 20 OFFSET 0
```

                         ↓
```
┌────────────────────────────────────────────────────┐
│ Database returns Page<Student>                     │
└────────────────────────────────────────────────────┘
                         ↓
```

**Full Request Flow:**
```
Request → Controller → Service → Mapper → Repository → Database
  ↓         ↓           ↓         ↓          ↓           ↓
GET /api   Gets bean   Gets bean Gets bean Gets bean  Executes
/students  from        from      from      from        SQL
           container   container container container
```

### Why Beans are Reused:

```java
// First request
Request 1 → Uses StudentController bean instance → StudentService bean instance

// Second request (same second, different user)
Request 2 → Uses SAME StudentController bean → SAME StudentService bean

// Beans are singletons by default
// Spring creates them ONCE during startup
// Reuses them for ALL requests
```

---

## Phase 8: Destruction Phase

### What Happens?
When Spring application shuts down, beans are destroyed in reverse order.

### Three Ways to Destroy:

#### 1. @PreDestroy (Preferred)

```java
@Service
public class StudentService {
    
    @PostConstruct
    public void init() {
        System.out.println("Service initialized");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("Service being destroyed!");
        
        // Close resources
        // Cleanup cache
        // Close DB connections
        // Save state
    }
}
```

#### 2. DisposableBean Interface

```java
@Component
public class StudentMapperWithDisposable 
        implements DisposableBean {
    
    @Override
    public void destroy() throws Exception {
        System.out.println("Mapper being destroyed");
    }
}
```

#### 3. destroy-method in XML (Legacy)

```xml
<bean id="studentService" 
      class="com.students.crud.Services.StudentService"
      destroy-method="cleanup">
</bean>
```

```java
@Service
public class StudentService {
    public void cleanup() {
        // Called during destruction
    }
}
```

### When Are Beans Destroyed?

**In a Spring Boot Application:**

```java
// Starting the app
public class CrudApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(CrudApplication.class, args);
        
        // Application runs...
        
        // When we call shutdown:
        context.close();  // ← Triggers destruction phase
    }
}
```

**In Production (Graceful Shutdown):**

```
1. Kill signal received (SIGTERM)
2. Spring Boot stops accepting new requests
3. Waits for in-flight requests to complete
4. Calls @PreDestroy on all beans
5. JVM shuts down
```

### Destruction Order (Reverse of Creation):

```
┌──────────────────────────────────────┐
│ DESTRUCTION PHASE (Reverse Order)    │
├──────────────────────────────────────┤
│ 1. RedisCacheManager.destroy()       │
│ 2. RedisConnectionFactory.destroy()  │
│ 3. StudentController.destroy()       │
│ 4. StudentService.destroy()          │
│ 5. StudentMapper.destroy()           │
│ 6. StudentRepository.destroy()       │
│ 7. Close DB connections              │
│ 8. JVM shuts down                    │
└──────────────────────────────────────┘
```

---

## Complete Bean Lifecycle Timeline

### For StudentService Bean:

```
TIME    EVENT                                   CODE
────────────────────────────────────────────────────────────────
 |
 ├─> 1. COMPONENT SCAN
 │      Spring finds @Service annotation        @Service
 │      Registers BeanDefinition               public class StudentService
 │
 ├─> 2. INSTANTIATION
 │      new StudentService(...) is called       StudentService s = new StudentService();
 │
 ├─> 3. DEPENDENCY INJECTION
 │      Constructor params populated           studentRepository = container.getBean(...)
 │      studentMapper = container.getBean(...) studentMapper = container.getBean(...)
 │
 ├─> 4. AWARE INTERFACES
 │      (if implemented)                       setBeanName("studentService")
 │                                             setApplicationContext(...)
 │
 ├─> 5. BeanPostProcessor.BEFORE
 │      Pre-initialization logic               postProcessBeforeInitialization()
 │
 ├─> 6. INITIALIZATION
 │      @PostConstruct methods run             init() method runs
 │      InitializingBean.afterPropertiesSet()  afterPropertiesSet() called
 │
 ├─> 7. BeanPostProcessor.AFTER
 │      Post-initialization logic              postProcessAfterInitialization()
 │      Proxying happens (@Cacheable, etc.)    Caching proxy created
 │
 ├─> 8. READY FOR USE
 │      Bean ready to serve requests           ✓ Injection complete
 │                                             ✓ All listeners attached
 │                                             ✓ Cache configured
 │
 ├─> 9. API REQUESTS HANDLED
 │      Bean used to process requests          getStudents() called
 │                                             searchStudentsByName() called
 │
 ├─> 10. SHUTDOWN SIGNAL
 │       Application closing                   Ctrl+C or kill signal
 │
 ├─> 11. DESTRUCTION
 │       @PreDestroy methods run               destroy() method runs
 │       DisposableBean.destroy() called       destroy() called
 │
 └─> 12. GARBAGE COLLECTION
         Bean removed from memory               StudentService = null
```

---

## Summary: The Exact Execution Order

### Startup Sequence:

```
1. ApplicationContext starts
2. Component Scan finds all @Component, @Service, @Repository, @Controller
3. BeanDefinitions created for each class
4. Beans instantiated in dependency order:
   a. Classes with no dependencies (StudentRepository, StudentMapper)
   b. Classes depending on above (StudentService)
   c. Classes depending on above (StudentController)
5. Constructor Injection: Dependencies passed to constructors
6. Aware interfaces called: setBeanName(), setApplicationContext()
7. BeanPostProcessor.postProcessBeforeInitialization()
8. Initialization methods called: @PostConstruct, afterPropertiesSet()
9. BeanPostProcessor.postProcessAfterInitialization()
   - @Cacheable proxies created
   - @Transactional proxies created
   - @Async proxies created
10. Beans ready for requests
11. Application listening on port 8080
```

### Request Handling:

```
1. HTTP Request arrives
2. DispatcherServlet routes to correct Controller
3. Controller bean retrieved from container (singleton)
4. Service bean retrieved from container (singleton)
5. Repository bean retrieved from container
6. @Cacheable cache checked
7. Repository queries database
8. Data transformed by Mapper
9. Response returned to client
```

### Shutdown Sequence:

```
1. Kill signal received
2. Stop accepting new requests
3. Wait for in-flight requests to complete
4. @PreDestroy methods called on all beans (reverse order)
5. DisposableBean.destroy() called
6. Database connections closed
7. Redis connections closed
8. JVM terminates
```

---

## Key Interview Points

### Q: When are beans created?
**A:** During ApplicationContext startup, not during request time. Beans are singletons by default.

### Q: Why is Constructor Injection better?
**A:** Makes dependencies explicit, allows immutable fields, fails fast if dependencies are missing.

### Q: How does @Autowired work?
**A:** Spring resolves dependency types from the container and injects them.

### Q: What's the difference between @Autowired and Constructor Injection?
**A:** Constructor injection is done automatically without @Autowired. It's cleaner and safer.

### Q: When is @PostConstruct called?
**A:** After constructor and dependency injection, before the bean is used.

### Q: Can you modify a bean after it's created?
**A:** Yes, via BeanPostProcessor. Spring uses this for proxying (@Cacheable, @Transactional).

### Q: What happens if a dependency is not found?
**A:** Application startup fails with NoSuchBeanDefinitionException.

### Q: Are beans thread-safe?
**A:** Beans are singletons, so you shouldn't store request-specific data in them. Use ThreadLocal or scoped beans.

### Q: How many StudentService beans exist?
**A:** Only ONE (singleton scope). All requests use the same instance.

---

## Visual Summary

```
┌─────────────────────────────────────────────────────────────┐
│                    SPRING BOOT LIFECYCLE                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  STARTUP                                                    │
│  ├─ @ComponentScan finds classes                          │
│  ├─ BeanDefinitions created                               │
│  ├─ Beans instantiated (constructor)                      │
│  ├─ Dependencies injected                                 │
│  ├─ Aware interfaces called                               │
│  ├─ @PostConstruct / init methods                         │
│  └─ Beans ready ✓                                          │
│                                                              │
│  RUNTIME                                                    │
│  ├─ HTTP requests arrive                                  │
│  ├─ Controller → Service → Repository → DB               │
│  ├─ Beans reused for each request                         │
│  └─ Responses sent                                         │
│                                                              │
│  SHUTDOWN                                                   │
│  ├─ Kill signal received                                  │
│  ├─ In-flight requests complete                           │
│  ├─ @PreDestroy / destroy methods                         │
│  └─ JVM terminates                                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

