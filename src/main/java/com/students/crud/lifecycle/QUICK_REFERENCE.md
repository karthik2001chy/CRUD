# Spring Bean Lifecycle - Quick Reference Guide

## 1. COMPONENT SCANNING & BEAN DETECTION

### What Annotations Are Scanned?
```
@Component          → Generic component
@Service            → Business logic service
@Repository         → Data access layer
@Controller         → MVC controller
@RestController     → REST API controller
@Configuration      → Configuration class
```

### In Your CRUD App:
```
✓ StudentController       @RestController
✓ StudentService          @Service
✓ StudentRepository       @Repository (proxy created by Spring Data)
✓ StudentMapper           @Component
✓ RedisConfig             @Configuration
✓ OpenApiConfig           @Configuration
✓ CustomBeanPostProcessor @Component
```

### Timeline:
```
1. @SpringBootApplication found
2. @ComponentScan triggers
3. Classpath scanned for @Component, @Service, @Repository, etc.
4. BeanDefinition created for each class (no instance yet!)
5. Beans instantiated in dependency order
```

---

## 2. BEAN INSTANTIATION & DEPENDENCY INJECTION

### Order Matters (Dependency Order):
```
1. StudentRepository     (no dependencies)
2. StudentMapper         (no dependencies)
3. StudentService        (depends on 1, 2)
4. StudentController     (depends on 3)
5. Configuration beans
```

### Constructor Injection (Used in Your App):
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;  // FINAL!
    private final StudentMapper studentMapper;          // FINAL!

    // Spring calls this constructor during bean creation
    public StudentService(StudentRepository studentRepository,
                          StudentMapper studentMapper) {
        this.studentRepository = studentRepository;      // Injected
        this.studentMapper = studentMapper;              // Injected
    }
}
```

**Why Constructor Injection?**
- ✓ Immutable (final fields)
- ✓ Dependencies explicit in method signature
- ✓ Fails fast if dependency missing
- ✓ Easy to test (pass mocks to constructor)
- ✓ No @Autowired needed

### Alternative: Field Injection (NOT Recommended):
```java
@Service
public class StudentServiceBad {
    @Autowired  // ← Breaks immutability
    private StudentRepository studentRepository;
    
    // Problem: Can't use fields in constructor
    public StudentServiceBad() {
        // this.studentRepository = null here!
    }
}
```

---

## 3. BEAN AWARE INTERFACES

### BeanNameAware
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

### ApplicationContextAware
```java
@Component
public class MyBean implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext context) {
        // Now can access any bean from container at runtime
        StudentService service = context.getBean(StudentService.class);
    }
}
```

### Execution Order:
```
1. Constructor called
2. Dependency injection
3. setBeanName() ← BeanNameAware
4. setApplicationContext() ← ApplicationContextAware
5. BeanPostProcessor.postProcessBeforeInitialization()
6. @PostConstruct
7. InitializingBean.afterPropertiesSet()
8. BeanPostProcessor.postProcessAfterInitialization()
```

---

## 4. BEANPOSTPROCESSOR - BEFORE & AFTER INITIALIZATION

### What It Does:
```
Spring calls EVERY BeanPostProcessor for EVERY bean!

For StudentService bean:
├─ BeanPostProcessor.postProcessBeforeInitialization()
├─ @PostConstruct
├─ InitializingBean.afterPropertiesSet()
└─ BeanPostProcessor.postProcessAfterInitialization()
   └─ Creates proxy for @Cacheable, @Transactional, @Async
```

### How Spring Uses BeanPostProcessor:

```java
// BeanPostProcessor for @Cacheable caching

@PostConstruct
public void init() { }  // Your init code

// AFTER this, BeanPostProcessor creates a proxy:

StudentService original = bean;
StudentService proxy = new CachingProxy(original) {
    @Override
    public Page<StudentSimpleDto> getStudentsSimple(Pageable p) {
        if (cache.contains(key)) {
            return cache.get(key);      // Cache HIT
        }
        
        Page<StudentSimpleDto> result = original.getStudentsSimple(p);
        cache.put(key, result);
        return result;
    }
};

return proxy;  // Spring returns proxy instead of original
```

---

## 5. INITIALIZATION PHASE

### Three Ways to Initialize:

#### 1. @PostConstruct (PREFERRED)
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    
    @PostConstruct
    public void init() {
        System.out.println("Initialization!");
        // All dependencies available here
        // This is when to load cache, connect to services, etc.
    }
}
```

#### 2. InitializingBean (LEGACY)
```java
@Component
public class MyBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initialization!");
        // Same timing as @PostConstruct
    }
}
```

#### 3. init-method in XML (LEGACY - Not used in modern Spring Boot)
```xml
<bean id="bean" class="MyClass" init-method="initialize"/>
```

### When Is @PostConstruct Called?

```
Timeline:
┌──────────────────────────────┐
│ new StudentService(...)      │  Constructor called
│         ↓                    │
│ studentRepository injected   │  Dependencies set
│ studentMapper injected       │
│         ↓                    │
│ setBeanName() called         │  Aware interfaces
│ setApplicationContext()      │
│         ↓                    │
│ BeanPostProcessor.before()   │  Before initialization
│         ↓                    │
│ @PostConstruct init()        │ ← HAPPENS HERE!
│         ↓                    │
│ InitializingBean.after()     │
│         ↓                    │
│ BeanPostProcessor.after()    │  After initialization
│         ↓                    │
│ Bean ready to use!           │
└──────────────────────────────┘
```

---

## 6. BEAN USAGE IN API REQUESTS

### Request Flow:
```
HTTP Request
     ↓
GET /api/students?page=0&size=20
     ↓
DispatcherServlet routes to StudentController
     ↓
StudentController bean retrieved (SINGLETON - same instance!)
     ↓
studentController.listStudents(pageable) called
     ↓
this.studentService.getStudents(pageable) called
(this.studentService = already created bean from container)
     ↓
this.studentRepository.findAll(pageable) called
(this.studentRepository = already injected in constructor)
     ↓
SQL: SELECT * FROM students LIMIT 20 OFFSET 0
     ↓
Page<Student> returned to Controller
     ↓
ResponseEntity returned
     ↓
JSON sent to client
```

### Key Insight: SINGLETON PATTERN

```
Application runs for 1 hour, handles 10,000 requests

Request 1  → StudentController instance #1 → StudentService instance #1
Request 2  → StudentController instance #1 → StudentService instance #1
Request 3  → StudentController instance #1 → StudentService instance #1
...
Request 10000 → StudentController instance #1 → StudentService instance #1

ONE instance serves ALL requests!

Spring creates bean ONCE at startup
All requests reuse SAME instance
```

### Thread Safety:

```java
// ✓ SAFE - Local variables (stack)
@Service
public class StudentService {
    public Page<Student> getStudents(Pageable p) {
        List<Student> students = repository.findAll();  // Local variable
        // Each thread gets its own copy on stack
        return new PageImpl(students);
    }
}

// ✗ UNSAFE - Instance fields
@Service
public class StudentServiceBad {
    private List<Student> cache;  // SHARED by all threads!
    
    public void loadCache() {
        cache = repository.findAll();  // Multiple threads overwrite!
    }
}
```

---

## 7. DESTRUCTION PHASE

### Three Ways to Cleanup:

#### 1. @PreDestroy (PREFERRED)
```java
@Service
public class StudentService {
    @PreDestroy
    public void cleanup() {
        System.out.println("Service being destroyed!");
        // Close database connections
        // Flush caches
        // Save state to file
        // Stop background threads
    }
}
```

#### 2. DisposableBean (LEGACY)
```java
@Component
public class MyBean implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        System.out.println("Bean destroyed!");
    }
}
```

#### 3. destroy-method in XML (LEGACY)
```xml
<bean id="bean" class="MyClass" destroy-method="cleanup"/>
```

### When Are Beans Destroyed?

```
Spring Application Lifecycle:
┌────────────────────────────┐
│ STARTUP                    │
├────────────────────────────┤
│ Components scanned         │
│ Beans instantiated         │
│ Dependencies injected      │
│ @PostConstruct called      │
│ Application running        │
├────────────────────────────┤
│ RUNTIME                    │
├────────────────────────────┤
│ Handling requests...       │
│ (hours, days, weeks)       │
├────────────────────────────┤
│ SHUTDOWN                   │
├────────────────────────────┤
│ Kill signal received       │
│ (Ctrl+C or kill)           │
│ Stop accepting new requests│
│ Wait for in-flight requests│
│ @PreDestroy called ← HERE  │
│ DisposableBean.destroy()   │
│ Database connections close │
│ JVM shuts down             │
└────────────────────────────┘
```

### Destruction Order:

```
Reverse of creation order!

Created:
1. StudentRepository
2. StudentMapper
3. StudentService
4. StudentController

Destroyed:
1. StudentController
2. StudentService
3. StudentMapper
4. StudentRepository
```

---

## COMPLETE BEAN LIFECYCLE TIMELINE

### For StudentService Bean:

```
TIME    PHASE                              EVENT
────────────────────────────────────────────────────────────────
 ┌─────────────────────────────────────────────────────────────┐
 │ COMPONENT SCAN PHASE                                        │
 ├─────────────────────────────────────────────────────────────┤
 │ Spring finds @Service annotation                            │
 │ BeanDefinition created (not instantiated yet)               │
 │ Metadata collected about class, dependencies, annotations   │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ INSTANTIATION PHASE                                         │
 ├─────────────────────────────────────────────────────────────┤
 │ new StudentService(...) is called                           │
 │ No-arg constructor invoked (or with params for DI)          │
 │ Object created in heap memory                               │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ DEPENDENCY INJECTION PHASE                                  │
 ├─────────────────────────────────────────────────────────────┤
 │ Constructor parameters filled from container                │
 │ studentRepository = container.getBean(StudentRepository)    │
 │ studentMapper = container.getBean(StudentMapper)            │
 │ Both assigned to instance fields                            │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ AWARE INTERFACE PHASE                                       │
 ├─────────────────────────────────────────────────────────────┤
 │ IF implements BeanNameAware:                                │
 │     setBeanName("studentService")                           │
 │ IF implements ApplicationContextAware:                      │
 │     setApplicationContext(appContext)                       │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ BEANPOSTPROCESSOR BEFORE PHASE                              │
 ├─────────────────────────────────────────────────────────────┤
 │ postProcessBeforeInitialization() called                    │
 │ Can modify bean before initialization                       │
 │ Logging, validation, etc.                                   │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ INITIALIZATION PHASE                                        │
 ├─────────────────────────────────────────────────────────────┤
 │ 1. @PostConstruct init() method called                      │
 │    ├─ Load cache from database                              │
 │    ├─ Establish connections                                 │
 │    └─ Setup monitoring                                      │
 │                                                              │
 │ 2. InitializingBean.afterPropertiesSet() called             │
 │    (legacy, rarely used)                                    │
 │                                                              │
 │ 3. init-method called (XML only, rarely used)               │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ BEANPOSTPROCESSOR AFTER PHASE                               │
 ├─────────────────────────────────────────────────────────────┤
 │ postProcessAfterInitialization() called                     │
 │ Proxies created for:                                        │
 │ - @Cacheable (caching proxy)                                │
 │ - @Transactional (transaction proxy)                        │
 │ - @Async (async proxy)                                      │
 │ BeanPostProcessor returns proxy instead of original         │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ READY STATE                                                 │
 ├─────────────────────────────────────────────────────────────┤
 │ ✓ Bean fully initialized                                    │
 │ ✓ All dependencies available                                │
 │ ✓ Proxies created                                           │
 │ ✓ Ready to serve requests!                                  │
 └─────────────────────────────────────────────────────────────┘
            ↓ (Many requests use this bean)
 ┌─────────────────────────────────────────────────────────────┐
 │ REQUEST HANDLING PHASE (repeated many times)                │
 ├─────────────────────────────────────────────────────────────┤
 │ Request arrives → Service bean called → DB query executed   │
 │ Request arrives → Service bean called → DB query executed   │
 │ Request arrives → Service bean called → DB query executed   │
 │ (Same bean instance used for all requests!)                 │
 └─────────────────────────────────────────────────────────────┘
            ↓ (Application shutdown signal)
 ┌─────────────────────────────────────────────────────────────┐
 │ DESTRUCTION PHASE                                           │
 ├─────────────────────────────────────────────────────────────┤
 │ @PreDestroy cleanup() method called                         │
 │ ├─ Close database connections                               │
 │ ├─ Flush caches                                             │
 │ ├─ Save state                                               │
 │ └─ Stop threads                                             │
 │                                                              │
 │ DisposableBean.destroy() called (if implemented)            │
 │                                                              │
 │ destroy-method called (XML only)                            │
 └─────────────────────────────────────────────────────────────┘
            ↓
 ┌─────────────────────────────────────────────────────────────┐
 │ GARBAGE COLLECTION                                          │
 ├─────────────────────────────────────────────────────────────┤
 │ Bean removed from ApplicationContext                        │
 │ No references remain                                        │
 │ Garbage collector frees memory                              │
 │ JVM shuts down                                              │
 └─────────────────────────────────────────────────────────────┘
```

---

## INTERVIEW QUICK ANSWERS

**Q: What are Spring beans?**
A: Objects managed by Spring Container. Created, configured, and destroyed by Spring.

**Q: When are beans created?**
A: During application startup in dependency order, before requests arrive.

**Q: How many instances of StudentService exist?**
A: ONE (singleton scope). All requests use the same instance.

**Q: Why is constructor injection better than field injection?**
A: Immutable (final), explicit dependencies, testable, fails fast.

**Q: What's @PostConstruct for?**
A: Initialize bean after construction and dependency injection. Not in constructor.

**Q: How does @Cacheable work?**
A: BeanPostProcessor creates a proxy that checks cache before calling method.

**Q: What if a dependency is not found?**
A: NoSuchBeanDefinitionException. Application startup fails.

**Q: Are beans thread-safe?**
A: Yes, because they're stateless. Don't store request data in instance fields.

**Q: When are @PreDestroy methods called?**
A: On application shutdown (Ctrl+C or kill signal).

**Q: Can you manually create beans?**
A: Yes, with @Bean methods in @Configuration classes.

---

## VISUAL SUMMARY

```
┌──────────────────────────────────────────────────────┐
│           SPRING BEAN LIFECYCLE                      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  STARTUP PHASE                                      │
│  ├─ Component Scan (@Component, @Service, etc.)    │
│  ├─ Bean Definition created                         │
│  ├─ Instantiation (constructor called)              │
│  ├─ Dependency Injection                            │
│  ├─ Aware Interfaces (setBeanName, etc.)           │
│  ├─ BeanPostProcessor.before()                     │
│  ├─ Initialization (@PostConstruct, etc.)          │
│  ├─ BeanPostProcessor.after() [Proxy creation]     │
│  └─ Bean ready! ✓                                   │
│                                                      │
│  RUNTIME PHASE                                      │
│  ├─ HTTP requests arrive                            │
│  ├─ Controller → Service → Repository → DB         │
│  ├─ Beans reused for all requests                   │
│  └─ Responses sent                                  │
│                                                      │
│  SHUTDOWN PHASE                                     │
│  ├─ Kill signal (Ctrl+C)                            │
│  ├─ @PreDestroy / destroy() methods called          │
│  ├─ Resources cleaned up                            │
│  └─ JVM terminates                                  │
│                                                      │
└──────────────────────────────────────────────────────┘
```

---

## KEY TAKEAWAYS

1. **Beans are created at STARTUP, not at request time**
2. **ONE instance serves ALL requests (singleton scope)**
3. **Dependency injection happens in CONSTRUCTOR**
4. **@PostConstruct called AFTER construction and injection**
5. **BeanPostProcessor creates PROXIES for @Cacheable, @Transactional**
6. **@PreDestroy called on APPLICATION SHUTDOWN**
7. **Order matters - dependencies created BEFORE dependents**
8. **Constructor Injection is BEST practice**
9. **Beans are stateless and reusable**
10. **Spring manages entire lifecycle automatically**

