# Spring Bean Lifecycle - Interview Questions & Answers

## Easy Questions

### Q1: What is a Spring Bean?
**A:** A Spring Bean is a Java object that is instantiated, assembled, and managed by the Spring IoC (Inversion of Control) container. Spring creates, configures, and destroys beans according to their lifecycle.

**Example from your app:**
```java
@Service
public class StudentService {
    // This is a Spring Bean!
    // Spring creates it, injects dependencies, and manages its lifecycle
}
```

### Q2: How does Spring detect beans?
**A:** Spring scans the classpath at startup for classes annotated with:
- `@Component` - Generic component
- `@Service` - Service layer bean
- `@Repository` - Data access layer bean
- `@Controller` / `@RestController` - Web layer bean
- `@Configuration` - Configuration bean

**In your CRUD app:**
```
✓ StudentController (@RestController)
✓ StudentService (@Service)
✓ StudentRepository (@Repository)
✓ StudentMapper (@Component)
✓ RedisConfig (@Configuration)
```

### Q3: When are Spring beans created?
**A:** During application startup, NOT during request time. All beans are created in dependency order before the application starts handling requests.

**Timeline:**
```
Application starts
  ↓
Component scan (finds @Service, @Component, etc.)
  ↓
Beans created in dependency order
  ↓
@PostConstruct methods called
  ↓
Application ready to receive requests
```

### Q4: What is dependency injection?
**A:** It's the process of Spring providing a bean's dependencies from the container. Instead of creating dependencies yourself, Spring injects them.

**In your CRUD app:**
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;  // Injected
    private final StudentMapper studentMapper;          // Injected

    // Spring calls this constructor and passes dependencies
    public StudentService(StudentRepository studentRepository,
                          StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }
}
```

### Q5: What are the three types of dependency injection?
**A:**
1. **Constructor Injection** (PREFERRED) - Dependencies in constructor
2. **Setter Injection** - Dependencies set via setter methods
3. **Field Injection** - Dependencies with @Autowired annotation

**Constructor Injection (Best):**
```java
public StudentService(StudentRepository repository) {
    this.repository = repository;  // Injected in constructor
}
```

**Setter Injection:**
```java
public class StudentService {
    private StudentRepository repository;
    
    public void setRepository(StudentRepository repository) {
        this.repository = repository;  // Injected via setter
    }
}
```

**Field Injection (Not Recommended):**
```java
public class StudentService {
    @Autowired
    private StudentRepository repository;  // Injected via field
    // Problem: Can't use in constructor, can be modified
}
```

---

## Medium Questions

### Q6: Explain the complete Spring Bean Lifecycle
**A:** The lifecycle has 5 main phases:

**1. Scanning:** Spring finds `@Component`, `@Service`, `@Repository`, etc.
```java
@Service
public class StudentService { }  // ← Spring finds this
```

**2. Instantiation:** Bean object created via constructor
```
new StudentService(...)  // Constructor called
```

**3. Dependency Injection:** Dependencies passed to constructor
```
StudentService service = new StudentService(
    container.getBean(StudentRepository.class),
    container.getBean(StudentMapper.class)
);
```

**4. Initialization:** Bean is configured and made ready
```
@PostConstruct
public void init() {
    // Load cache, start threads, etc.
}
```

**5. Ready for Use:** Bean serves requests until shutdown
```
// Handles millions of requests
getStudents() called 1000s of times
```

**6. Destruction:** On shutdown, beans are cleaned up
```
@PreDestroy
public void cleanup() {
    // Close connections, save state
}
```

### Q7: What is @PostConstruct and when is it called?
**A:** `@PostConstruct` marks a method that runs AFTER constructor and dependency injection complete. It's used to initialize the bean.

**Timeline:**
```
1. Constructor called
2. Dependencies injected
3. @PostConstruct method called ← HERE
4. Bean ready to use
```

**In your CRUD app:**
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
        System.out.println("1. Constructor done");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("2. @PostConstruct called");
        System.out.println("3. Dependencies available: " + studentRepository);
        // Good place to load cache, connect to services, etc.
    }
}
```

### Q8: What is the difference between @Component and @Service?
**A:** They're functionally the same. `@Service` is semantically more specific for business logic.

```java
@Component
public class StudentMapper {
    // Generic utility component
}

@Service
public class StudentService {
    // Business logic service - more specific
}

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // Data access - even more specific
}

@RestController
public class StudentController {
    // Web controller - REST endpoints
}
```

### Q9: How does Spring create a repository interface like StudentRepository?
**A:** Spring Data JPA creates a dynamic proxy implementation at runtime.

```java
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // Spring creates a proxy that implements this interface
    // Provides implementations for findAll(), save(), delete(), etc.
}
```

**What Spring does internally:**
```java
class StudentRepositoryImpl implements StudentRepository {
    @Override
    public Page<Student> findAll(Pageable pageable) {
        // Auto-generated SQL
        // SELECT * FROM students LIMIT :limit OFFSET :offset
    }
    
    @Override
    public void deleteById(String id) {
        // Auto-generated SQL
        // DELETE FROM students WHERE registration_no = :id
    }
}
```

### Q10: What is the difference between singleton and prototype scope?
**A:** 
- **Singleton (default):** ONE instance shared by all requests
- **Prototype:** NEW instance created for each request

```java
// Singleton (default)
@Service  // Singleton scope
public class StudentService {
    // ONE instance for entire application
}

// Prototype
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeService {
    // NEW instance created for each injection
}
```

**Comparison:**
```
Singleton:
Request 1 → Uses StudentService instance #1
Request 2 → Uses StudentService instance #1 (SAME!)
Request 3 → Uses StudentService instance #1 (SAME!)

Prototype:
Request 1 → Gets StudentService instance #1
Request 2 → Gets StudentService instance #2 (NEW!)
Request 3 → Gets StudentService instance #3 (NEW!)
```

---

## Hard Questions

### Q11: What is a BeanPostProcessor and how does Spring use it?
**A:** A BeanPostProcessor allows you to hook into the bean lifecycle and modify beans before and after initialization. Spring uses it internally to create proxies for `@Cacheable`, `@Transactional`, `@Async`, etc.

**Example from your CRUD app:**

When Spring sees `@Cacheable` on a method:
```java
@Service
public class StudentService {
    @Cacheable(value = "studentSimple", key = "#pageable...")
    public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
        return studentRepository.findAll(pageable).map(studentMapper::toSimpleDto);
    }
}
```

Spring's BeanPostProcessor does this:
```java
public class CachingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean has @Cacheable methods) {
            // Create caching proxy
            StudentService proxy = new CachingProxy(bean) {
                @Override
                public Page<StudentSimpleDto> getStudentsSimple(Pageable p) {
                    String key = "studentSimple:" + p.getPageNumber();
                    
                    // Check cache first
                    if (cache.containsKey(key)) {
                        System.out.println("Cache HIT");
                        return cache.get(key);
                    }
                    
                    // Cache miss - call original
                    Page<StudentSimpleDto> result = 
                        originalService.getStudentsSimple(p);
                    
                    // Store in Redis cache
                    cache.put(key, result);
                    
                    return result;
                }
            };
            
            return proxy;  // Return proxy instead of original
        }
        
        return bean;
    }
}
```

**Execution Order:**
```
1. Constructor called
2. Dependencies injected
3. BeanPostProcessor.postProcessBeforeInitialization()
4. @PostConstruct methods called
5. InitializingBean.afterPropertiesSet()
6. BeanPostProcessor.postProcessAfterInitialization()  ← Proxy created here
7. Bean ready to use
```

### Q12: How does Spring know which constructor to use when there are multiple constructors?
**A:** Spring uses these rules:

1. **One constructor with @Autowired** - Use that one
2. **One constructor** - Use that one automatically (no @Autowired needed)
3. **Multiple constructors, none with @Autowired** - Error (ambiguous)
4. **No-arg constructor** - Use that one if others are present

**In your CRUD app:**
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    // Spring automatically uses this (only constructor)
    // No @Autowired needed!
    public StudentService(StudentRepository studentRepository,
                          StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }
}
```

**Ambiguous case (would fail):**
```java
@Service
public class StudentService {
    // ERROR: Spring doesn't know which to use!
    public StudentService(StudentRepository repository) { }
    public StudentService(StudentMapper mapper) { }
    public StudentService(StudentRepository r, StudentMapper m) { }
}
```

### Q13: What happens if a required dependency cannot be found?
**A:** Spring throws `NoSuchBeanDefinitionException` and the application fails to start.

**Example:**
```java
@Service
public class StudentService {
    public StudentService(NonExistentService service) {
        // If NonExistentService bean doesn't exist:
        // Exception: NoSuchBeanDefinitionException
        // Application startup FAILS
    }
}
```

**This is GOOD because:**
- You know immediately if dependencies are missing
- Fails fast, not at runtime
- Prevents null pointer exceptions

### Q14: How are beans destroyed and when?
**A:** When the application shuts down, Spring calls destruction methods in reverse creation order.

**Three destruction methods:**

1. **@PreDestroy (Preferred)**
```java
@Service
public class StudentService {
    @PreDestroy
    public void cleanup() {
        System.out.println("Destroying StudentService");
        // Close DB connections
        // Close Redis connections
        // Stop background threads
        // Save state
    }
}
```

2. **DisposableBean Interface**
```java
@Component
public class MyBean implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        // Called on shutdown
    }
}
```

3. **destroy-method in XML** (Legacy)
```xml
<bean id="bean" class="MyClass" destroy-method="cleanup"/>
```

**Destruction Order (Reverse of Creation):**
```
Created:
1. StudentRepository
2. StudentMapper
3. StudentService
4. StudentController

Destroyed (Reverse):
1. StudentController @PreDestroy
2. StudentService @PreDestroy
3. StudentMapper @PreDestroy
4. StudentRepository @PreDestroy
```

### Q15: What are Aware interfaces and how do they work?
**A:** Aware interfaces let beans know about their container context. Spring calls setter methods for these interfaces if a bean implements them.

**Common Aware interfaces:**

1. **BeanNameAware** - Know your own bean name
```java
@Component
public class MyBean implements BeanNameAware {
    @Override
    public void setBeanName(String name) {
        System.out.println("My bean name: " + name);
        // Output: myBean (lowercase)
    }
}
```

2. **ApplicationContextAware** - Access the Spring container
```java
@Component
public class MyBean implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext context) {
        // Now can get any bean
        StudentService service = context.getBean(StudentService.class);
    }
}
```

3. **BeanFactoryAware** - Access the bean factory
```java
@Component
public class MyBean implements BeanFactoryAware {
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        // Can get beans from factory
    }
}
```

**Execution Order:**
```
1. Constructor
2. Dependency injection
3. BeanNameAware.setBeanName()
4. ApplicationContextAware.setApplicationContext()
5. BeanFactoryAware.setBeanFactory()
6. BeanPostProcessor.before()
7. @PostConstruct
8. BeanPostProcessor.after()
```

---

## Very Hard / Expert Questions

### Q16: How would you debug a circular dependency problem?
**A:** Circular dependencies occur when beans depend on each other.

**Example:**
```java
@Service
public class ServiceA {
    public ServiceA(ServiceB serviceB) {  // A needs B
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    public ServiceB(ServiceA serviceA) {  // B needs A (CIRCULAR!)
        this.serviceA = serviceA;
    }
}
```

**Error:**
```
Exception: Circular dependency detected
The dependencies of some of the beans in the application context form a cycle
```

**Solutions:**

1. **Refactor dependencies** - Remove the circular dependency
```java
// Better structure
@Service
public class ServiceA {
    public ServiceA(ServiceC serviceC) {
        this.serviceC = serviceC;
    }
}

@Service
public class ServiceB {
    public ServiceB(ServiceC serviceC) {
        this.serviceC = serviceC;
    }
}

@Service
public class ServiceC {
    // No dependencies - resolves cycle
}
```

2. **Use @Lazy** - Delay injection
```java
@Service
public class ServiceA {
    public ServiceA(@Lazy ServiceB serviceB) {
        // ServiceB not created immediately
        this.serviceB = serviceB;
    }
}
```

3. **Use setter injection instead**
```java
@Service
public class ServiceA {
    private ServiceB serviceB;
    
    @Autowired
    public void setServiceB(ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}
```

### Q17: How does Spring handle the request scope vs singleton scope in a multi-threaded environment?
**A:** Spring handles multi-threading through thread-local storage and request scoping.

**Singleton beans (default):**
```
Thread 1 (Request 1) → StudentService instance #1
Thread 2 (Request 2) → StudentService instance #1 (SAME)
Thread 3 (Request 3) → StudentService instance #1 (SAME)

Problem: Shared mutable state causes issues
```

**Solution: Use local variables**
```java
@Service
public class StudentService {  // Singleton
    public Page<Student> getStudents(Pageable p) {
        // Local variables - each thread gets its own copy
        Page<Student> page = repository.findAll(p);  // Safe
        List<Student> students = page.getContent();  // Safe
        return page;
    }
}
```

**Request scope for thread safety:**
```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class UserContext {
    // NEW instance created for each request
    // Thread-safe because each thread has its own instance
    private User currentUser;
    
    public User getCurrentUser() {
        return currentUser;
    }
}
```

**Then inject it:**
```java
@Service
public class StudentService {
    private final UserContext userContext;  // Request-scoped
    
    public Page<Student> getStudents(Pageable p) {
        User user = userContext.getCurrentUser();  // Thread-safe
        // ...
    }
}
```

### Q18: Can you explain how @Transactional uses BeanPostProcessor?
**A:** Spring creates a proxy around methods with `@Transactional` to manage transactions.

```java
@Service
public class StudentService {
    @Transactional  // Spring wraps this method
    public Student registerStudent(Student student) {
        return repository.save(student);
    }
}
```

**What Spring's BeanPostProcessor does:**
```java
StudentService original = bean;
StudentService proxy = new TransactionalProxy(original) {
    @Override
    public Student registerStudent(Student student) {
        // Start transaction
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Call original method
            Student result = original.registerStudent(student);
            
            // Commit transaction
            conn.commit();
            return result;
        } catch (Exception e) {
            // Rollback transaction
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }
};

return proxy;
```

---

## Scenario-Based Questions

### Q19: Walk me through what happens when application starts and a GET request arrives
**A:** 

**Application Startup:**
```
1. java -jar application.jar
2. Spring loads CrudApplication class
3. Component scan triggered (@ComponentScan automatic)
4. Finds:
   - StudentController (@RestController)
   - StudentService (@Service)
   - StudentRepository (@Repository)
   - StudentMapper (@Component)
   - RedisConfig (@Configuration)

5. Creates StudentRepository (no dependencies)
6. Creates StudentMapper (no dependencies)
7. Creates StudentService(repository, mapper)
8. Creates StudentController(service)
9. Calls @PostConstruct methods
10. Creates proxies for @Cacheable, @Transactional
11. Server starts on port 8080
12. Waiting for requests...
```

**Request Arrives:**
```
GET /api/students?page=0&size=20

1. HTTP request hits Spring
2. DispatcherServlet receives it
3. Maps to StudentController.listStudents()
4. Gets StudentController bean from container
   (SINGLETON - same instance used)
5. Calls: controller.listStudents(pageable)
6. Controller calls: this.studentService.getStudents(pageable)
   (this.studentService - same bean, already injected)
7. Service calls: this.studentRepository.findAll(pageable)
8. Repository executes SQL:
   SELECT * FROM students LIMIT 20 OFFSET 0
9. Database returns data
10. Repository returns Page<Student>
11. Service returns to controller
12. Controller returns ResponseEntity
13. Spring converts to JSON
14. HTTP response sent to client

Client receives:
{
  "content": [...],
  "totalElements": 100,
  "pageNumber": 0,
  "pageSize": 20
}
```

### Q20: What happens if you call a service method from the constructor?
**A:** Generally works, but can cause issues if the service uses @Cacheable.

**Why it might fail:**
```java
@Service
public class StudentService {
    private final StudentRepository repository;
    
    public StudentService(StudentRepository repository) {
        this.repository = repository;
        
        // Calling method here
        List<Student> students = this.getAllSimple();  // Problem!
    }
    
    @Cacheable(value = "students")
    public List<Student> getAllSimple() {
        return repository.findAll();
    }
}
```

**Issue:**
```
1. Constructor called
2. this.getAllSimple() called
3. But proxy NOT YET created!
4. Caching doesn't work
5. Direct method called, no caching

Later in @PostConstruct:
1. Proxy is created
2. getAllSimple() now uses caching
3. Inconsistent behavior!
```

**Solution: Use @PostConstruct instead**
```java
@Service
public class StudentService {
    private final StudentRepository repository;
    private List<Student> cachedStudents;
    
    public StudentService(StudentRepository repository) {
        this.repository = repository;
        // Don't call methods here
    }
    
    @PostConstruct
    public void init() {
        // Now safe to call methods
        this.cachedStudents = this.getAllSimple();  // Caching works!
    }
    
    @Cacheable(value = "students")
    public List<Student> getAllSimple() {
        return repository.findAll();
    }
}
```

---

## Summary Comparison Table

| Feature | Singleton | Prototype |
|---------|-----------|-----------|
| **Instances** | 1 per app | New per injection |
| **Scope** | Application-wide | Per request |
| **Thread-safe?** | Stateless design needed | Naturally thread-safe |
| **Memory** | Low | Higher |
| **Performance** | Better | Slower (creation overhead) |
| **Default?** | Yes | No |

| Method | Constructor | @PostConstruct | @PreDestroy |
|--------|-------------|---|---|
| **When?** | First | After injection | On shutdown |
| **Initialization?** | No | Yes | No |
| **Dependencies?** | Not yet | Yes | Yes |
| **Use case?** | Assign fields | Load cache | Cleanup |
| **Modern?** | Yes | Yes | Yes |

---

## Tips for Interview

1. **Use your CRUD app as examples** - The interviewer will appreciate real context
2. **Draw diagrams** - Lifecycle phases, request flow, etc.
3. **Mention "singleton" and "immutable"** - Shows understanding
4. **Know the order** - Scanning → Instantiation → Injection → Initialization → Ready
5. **Constructor injection is best** - Mention this for every injection question
6. **Proxies for @Cacheable/@Transactional** - Explain how BeanPostProcessor creates them
7. **Circular dependencies** - Know how to recognize and fix them
8. **Thread safety** - Use local variables, not instance fields
9. **Compare methods** - @PostConstruct vs InitializingBean, etc.
10. **Show code examples** - Don't just talk, show code snippets

