# Spring Bean Lifecycle - Visual Diagrams

## Complete Bean Lifecycle Flow Chart

```
┌────────────────────────────────────────────────────────────────────────┐
│                    SPRING APPLICATION STARTUP                         │
└────────────────────────────────────────────────────────────────────────┘

                              START
                                │
                                ▼
                    ┌─────────────────────┐
                    │   JVM starts        │
                    │   Application.main()│
                    │   runs              │
                    └─────────────────────┘
                                │
                                ▼
                    ┌─────────────────────────────────┐
                    │   Spring loads configuration    │
                    │   Enables @ComponentScan        │
                    │   (automatic with               │
                    │    @SpringBootApplication)      │
                    └─────────────────────────────────┘
                                │
                                ▼
         ┌──────────────────────────────────────────┐
         │   PHASE 1: COMPONENT SCANNING             │
         ├──────────────────────────────────────────┤
         │ Spring scans classpath for:               │
         │ • @Component                              │
         │ • @Service                                │
         │ • @Repository                             │
         │ • @Controller / @RestController           │
         │ • @Configuration                          │
         │                                           │
         │ ✓ StudentRepository                       │
         │ ✓ StudentMapper                           │
         │ ✓ StudentService                          │
         │ ✓ StudentController                       │
         │ ✓ RedisConfig                             │
         │                                           │
         │ BeanDefinitions created (no instance!)   │
         └──────────────────────────────────────────┘
                                │
                                ▼
      ┌────────────────────────────────────────────┐
      │   PHASE 2: BEAN INSTANTIATION (Order!)     │
      ├────────────────────────────────────────────┤
      │                                            │
      │ [1] StudentRepository (no dependencies)    │
      │     new StudentRepository()                │
      │                                            │
      │ [2] StudentMapper (no dependencies)        │
      │     new StudentMapper()                    │
      │                                            │
      │ [3] StudentService (depends on 1, 2)       │
      │     new StudentService(repo, mapper)       │
      │                                            │
      │ [4] StudentController (depends on 3)       │
      │     new StudentController(service)         │
      │                                            │
      │ [5] RedisConfig                            │
      │     new RedisConfig()                      │
      │                                            │
      └────────────────────────────────────────────┘
                                │
                                ▼
   ┌───────────────────────────────────────────────┐
   │   PHASE 3: DEPENDENCY INJECTION                │
   ├───────────────────────────────────────────────┤
   │ For each bean with constructor parameters:   │
   │                                               │
   │ StudentService constructor:                  │
   │ ├─ Parameter 1: StudentRepository            │
   │ │  └─ Found in container ✓                   │
   │ └─ Parameter 2: StudentMapper                │
   │    └─ Found in container ✓                   │
   │                                               │
   │ Result:                                       │
   │ studentService.studentRepository = repo      │
   │ studentService.studentMapper = mapper        │
   │                                               │
   │ StudentController constructor:               │
   │ └─ Parameter: StudentService                 │
   │    └─ Found in container ✓                   │
   │                                               │
   │ Result:                                       │
   │ controller.studentService = service          │
   └───────────────────────────────────────────────┘
                                │
                                ▼
    ┌──────────────────────────────────────────┐
    │   PHASE 4: AWARE INTERFACES               │
    ├──────────────────────────────────────────┤
    │ For beans implementing Aware interfaces: │
    │                                          │
    │ If BeanNameAware:                        │
    │ └─ setBeanName("studentService")         │
    │                                          │
    │ If ApplicationContextAware:              │
    │ └─ setApplicationContext(context)        │
    │                                          │
    │ If BeanFactoryAware:                     │
    │ └─ setBeanFactory(factory)               │
    └──────────────────────────────────────────┘
                                │
                                ▼
 ┌───────────────────────────────────────────────┐
 │   PHASE 5: BEANPOSTPROCESSOR.BEFORE()         │
 ├───────────────────────────────────────────────┤
 │ For each bean:                                │
 │                                               │
 │ postProcessBeforeInitialization()             │
 │ ├─ Can log bean creation                      │
 │ ├─ Can validate configuration                 │
 │ └─ Can modify bean properties                 │
 │                                               │
 │ Example logging:                              │
 │ "Creating bean: StudentService"               │
 └───────────────────────────────────────────────┘
                                │
                                ▼
  ┌──────────────────────────────────────────────┐
  │   PHASE 6: INITIALIZATION METHODS             │
  ├──────────────────────────────────────────────┤
  │ Execute in order:                            │
  │                                              │
  │ [1] @PostConstruct methods                   │
  │     ├─ Load cache from database              │
  │     ├─ Connect to external services          │
  │     ├─ Start background threads              │
  │     └─ Setup monitoring                      │
  │                                              │
  │ [2] InitializingBean.afterPropertiesSet()    │
  │     (legacy, rarely used)                    │
  │                                              │
  │ [3] init-method from XML                     │
  │     (legacy, rarely used in Boot)            │
  └──────────────────────────────────────────────┘
                                │
                                ▼
 ┌───────────────────────────────────────────────┐
 │   PHASE 7: BEANPOSTPROCESSOR.AFTER()          │
 ├───────────────────────────────────────────────┤
 │ For each bean:                                │
 │                                               │
 │ postProcessAfterInitialization()              │
 │ ├─ Create proxies for @Cacheable              │
 │ ├─ Create proxies for @Transactional          │
 │ ├─ Create proxies for @Async                  │
 │ └─ Return proxy instead of original           │
 │                                               │
 │ Example for StudentService:                  │
 │ StudentService original = bean               │
 │ StudentService proxy = new CachingProxy(...)  │
 │ return proxy;                                 │
 └───────────────────────────────────────────────┘
                                │
                                ▼
          ┌───────────────────────────────────┐
          │   BEAN READY! ✓                   │
          │                                   │
          │ • All instantiated                │
          │ • All dependencies injected       │
          │ • All initialization done         │
          │ • Proxies created                 │
          │ • Ready to handle requests        │
          └───────────────────────────────────┘
                                │
                                ▼
            ┌─────────────────────────────────┐
            │   APPLICATION STARTS LISTENING  │
            │   on port 8080                  │
            │                                 │
            │   Waiting for HTTP requests... │
            └─────────────────────────────────┘

```

---

## Bean Instantiation Order with Dependencies

```
                        DEPENDENCY GRAPH
                        
    StudentRepository ─────┐
    (Interface)            │
    No dependencies        │
                           │
                           ├──→ StudentService
    StudentMapper ─────────┤    (depends on both)
    (@Component)           │
    No dependencies        │
                           │
                           │
    StudentService ────────┴──→ StudentController
                                (depends on service)


    INSTANTIATION ORDER:
    
    [1] StudentRepository
        (first - no dependencies)
        
    [2] StudentMapper
        (second - no dependencies)
        
    [3] StudentService
        (third - depends on 1, 2)
        Needs: StudentRepository ✓ (created)
        Needs: StudentMapper ✓ (created)
        
    [4] StudentController
        (fourth - depends on 3)
        Needs: StudentService ✓ (created)
        
    [5] Configuration beans
        (RedisConfig, OpenApiConfig)
        Depends on: RedisCacheManager dependency
        
    
    KEY POINT:
    ══════════════════════════════════════════════════════
    A bean is only created after ALL its dependencies
    have been created first.
    
    If there's a circular dependency (A→B→C→A),
    Spring throws exception and startup fails.
    ══════════════════════════════════════════════════════
```

---

## HTTP Request Processing Flow

```
                        CLIENT
                          │
                          ▼
            ┌─────────────────────────────┐
            │  HTTP GET /api/students     │
            │  ?page=0&size=20            │
            │                             │
            │  Headers:                   │
            │  Accept: application/json   │
            └─────────────────────────────┘
                          │
                          ▼
        ┌──────────────────────────────────┐
        │   Spring DispatcherServlet       │
        │   (receives request)             │
        └──────────────────────────────────┘
                          │
                          ▼
        ┌──────────────────────────────────┐
        │   URL mapping:                   │
        │   /api/students                  │
        │   Maps to:                       │
        │   StudentController.listStudents │
        └──────────────────────────────────┘
                          │
                          ▼
    ┌────────────────────────────────────────┐
    │   Get StudentController bean            │
    │   from Spring Container                 │
    │                                         │
    │   Returns: SINGLETON instance           │
    │   (same instance for all requests)      │
    └────────────────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  CONTROLLER LAYER                        │
  ├──────────────────────────────────────────┤
  │  StudentController.listStudents()         │
  │  called with:                            │
  │  Pageable: {page: 0, size: 20}           │
  │                                          │
  │  this.studentService = already injected  │
  │                                          │
  │  Call: studentService.getStudents(page)  │
  └──────────────────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  SERVICE LAYER                           │
  ├──────────────────────────────────────────┤
  │  StudentService.getStudents()             │
  │                                          │
  │  @Cacheable annotation detected          │
  │  (BeanPostProcessor created proxy)       │
  │                                          │
  │  Proxy checks cache:                     │
  │  ├─ Is result in Redis? NO               │
  │  └─ Continue to method...                │
  │                                          │
  │  this.studentRepository.findAll(page)    │
  └──────────────────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  REPOSITORY LAYER                        │
  ├──────────────────────────────────────────┤
  │  StudentRepository.findAll(page)          │
  │                                          │
  │  Generates SQL:                          │
  │  SELECT * FROM students                  │
  │  LIMIT 20 OFFSET 0                       │
  │                                          │
  │  Executes query                          │
  └──────────────────────────────────────────┘
                          │
                          ▼
           ┌──────────────────────────────┐
           │  DATABASE                    │
           │                              │
           │  Returns results:            │
           │  [Student 1, Student 2, ...] │
           │  Page info:                  │
           │  - totalElements: 150        │
           │  - size: 20                  │
           │  - number: 0                 │
           └──────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  REPOSITORY RETURNS                      │
  │  Page<Student>                           │
  └──────────────────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  SERVICE LAYER PROXY                     │
  │                                          │
  │  Cache the result:                       │
  │  cache.put("studentPage:0:20",           │
  │           Page<Student>)                 │
  │                                          │
  │  Return to controller                    │
  └──────────────────────────────────────────┘
                          │
                          ▼
  ┌──────────────────────────────────────────┐
  │  CONTROLLER RETURNS                      │
  │  ResponseEntity<Page<Student>>           │
  │  with status 200 OK                      │
  └──────────────────────────────────────────┘
                          │
                          ▼
    ┌────────────────────────────────────────┐
    │  Spring DispatcherServlet               │
    │  Converts Java object to JSON           │
    │  Content-Type: application/json         │
    └────────────────────────────────────────┘
                          │
                          ▼
        ┌──────────────────────────────────┐
        │  HTTP Response (200 OK)          │
        │                                  │
        │  {                               │
        │    "content": [...],             │
        │    "totalElements": 150,         │
        │    "size": 20,                   │
        │    "number": 0,                  │
        │    "totalPages": 8               │
        │  }                               │
        └──────────────────────────────────┘
                          │
                          ▼
                        CLIENT
                    (receives response)
```

---

## Singleton vs Multiple Requests

```
SINGLETON SCOPE (default):

    ┌──────────────────────────────────────────┐
    │  Application Startup                     │
    ├──────────────────────────────────────────┤
    │  Creates: StudentService instance #1     │
    │  Memory address: 0x123ABC                │
    └──────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬──────────────┐
        │             │             │              │
        ▼             ▼             ▼              ▼
    Request 1     Request 2     Request 3    Request 1000
    (User A)      (User B)      (User C)     (User X)
        │             │             │              │
        └─────────────┼─────────────┴──────────────┘
                      │
          Uses SAME instance #1
          (0x123ABC)
                      │
        ┌─────────────┴─────────────┐
        │                           │
    Response to A              Response to B
    
    Result: ONE instance serves ALL requests!
    
    Pros:
    ✓ Memory efficient
    ✓ Fast (no creation overhead)
    ✓ Shared state (like cache)
    
    Cons:
    ✗ Must be thread-safe
    ✗ Can't store request-specific data
    
    
PROTOTYPE SCOPE:

    ┌──────────────────────────────────────────┐
    │  Application Startup                     │
    ├──────────────────────────────────────────┤
    │  Doesn't create instance yet             │
    └──────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬──────────────┐
        │             │             │              │
    Request 1     Request 2     Request 3    Request 1000
        │             │             │              │
        │ Creates     │ Creates     │ Creates      │ Creates
        │ instance #1 │ instance #2 │ instance #3  │ instance #1000
        │             │             │              │
        └─────────────┴─────────────┴──────────────┘
    
    Result: NEW instance for EACH request!
    
    Pros:
    ✓ Thread-safe by nature
    ✓ Can store request data
    
    Cons:
    ✗ Memory overhead
    ✗ Creation cost per request
    ✗ Slower


REQUEST SCOPE:

    ┌──────────────────────────────────────────┐
    │  Web Application                         │
    ├──────────────────────────────────────────┤
    │  @Scope(WebApplicationContext.SCOPE_     │
    │         REQUEST)                        │
    └──────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬──────────────┐
        │             │             │              │
    Request 1 - Thread 1          Request 2 - Thread 2
        │                             │
        │ Creates instance #1         │ Creates instance #2
        │ for this thread              │ for this thread
        │                             │
    ├─ Instance #1 used            ├─ Instance #2 used
    │  (thread-local)              │  (thread-local)
    │                              │
    └─ Data isolated               └─ Data isolated
```

---

## Bean Destruction Timeline

```
                    APPLICATION RUNNING
                    (hours/days/weeks)
                          │
                          │ Handling requests...
                          │
                          ▼
        ┌──────────────────────────────────┐
        │  User presses Ctrl+C             │
        │  OR                              │
        │  Kill signal received (SIGTERM)  │
        └──────────────────────────────────┘
                          │
                          ▼
        ┌──────────────────────────────────┐
        │  Spring detects shutdown signal  │
        │  (ApplicationContext.close())    │
        └──────────────────────────────────┘
                          │
                          ▼
     ┌────────────────────────────────────┐
     │  Stop accepting NEW requests       │
     │                                    │
     │  New requests rejected with:       │
     │  "Application is shutting down"    │
     └────────────────────────────────────┘
                          │
                          ▼
   ┌──────────────────────────────────────┐
   │  Wait for IN-FLIGHT requests         │
   │  to complete gracefully              │
   │                                      │
   │  Example:                            │
   │  Request 1 already running           │
   │  └─ Let it finish                    │
   │  Request 2 tries to arrive           │
   │  └─ Reject (shutdown in progress)    │
   └──────────────────────────────────────┘
                          │
                          ▼
 ┌────────────────────────────────────────┐
 │  PHASE 1: @PreDestroy METHODS           │
 ├────────────────────────────────────────┤
 │  (Reverse order of creation)           │
 │                                        │
 │  [1] StudentController @PreDestroy     │
 │  [2] StudentService @PreDestroy        │
 │      ├─ Close DB connections           │
 │      ├─ Close Redis connections        │
 │      ├─ Stop background threads        │
 │      └─ Flush caches                   │
 │  [3] StudentMapper @PreDestroy         │
 │  [4] StudentRepository @PreDestroy     │
 └────────────────────────────────────────┘
                          │
                          ▼
 ┌────────────────────────────────────────┐
 │  PHASE 2: DisposableBean.destroy()     │
 ├────────────────────────────────────────┤
 │  (If any bean implements interface)    │
 │                                        │
 │  Same reverse order                    │
 └────────────────────────────────────────┘
                          │
                          ▼
    ┌──────────────────────────────────┐
    │  PHASE 3: Resource Cleanup       │
    ├──────────────────────────────────┤
    │  Close all connections:          │
    │  ├─ Database connection pool     │
    │  ├─ Redis/Cache connection       │
    │  ├─ HTTP connections             │
    │  └─ Message queue connections    │
    └──────────────────────────────────┘
                          │
                          ▼
        ┌──────────────────────────────┐
        │  Remove beans from container │
        │  ApplicationContext closed   │
        └──────────────────────────────┘
                          │
                          ▼
       ┌───────────────────────────────┐
       │  Garbage Collection           │
       │  No references to beans       │
       │  Memory reclaimed             │
       └───────────────────────────────┘
                          │
                          ▼
         ┌─────────────────────────────┐
         │  JVM Shutdown               │
         │                             │
         │  Application terminated     │
         │  Process exit code: 0       │
         └─────────────────────────────┘
```

---

## Caching Proxy Creation

```
BEFORE BeanPostProcessor.after():

    ┌──────────────────────────────┐
    │  StudentService (ORIGINAL)   │
    ├──────────────────────────────┤
    │ getStudentsSimple() {         │
    │   return repo.findAll()       │
    │       .map(mapper::toDto);    │
    │ }                             │
    │                              │
    │ (Direct method call)          │
    └──────────────────────────────┘


BEANPOSTPROCESSOR.AFTER() - PROXY CREATION:

    Spring detects: @Cacheable on getStudentsSimple()
    
    Creates a PROXY:
    
    ┌─────────────────────────────────────────────┐
    │  StudentService (PROXY)                     │
    ├─────────────────────────────────────────────┤
    │ getStudentsSimple() {                       │
    │   // Check cache first                      │
    │   String key = "studentSimpleList";         │
    │   if (cache.containsKey(key)) {             │
    │     return cache.get(key);  // CACHE HIT!   │
    │   }                                         │
    │                                             │
    │   // Cache miss - call original             │
    │   Page<StudentSimpleDto> result =           │
    │     original.getStudentsSimple();  // →     │
    │                                    ├─ Call original method
    │   // Store in cache                         │
    │   cache.put(key, result);                   │
    │                                             │
    │   return result;  // CACHE MISS              │
    │ }                                           │
    └─────────────────────────────────────────────┘
                         │
                         │ Points to
                         ▼
    ┌──────────────────────────────┐
    │  ORIGINAL StudentService     │
    │  (Wrapped inside proxy)      │
    └──────────────────────────────┘


REQUEST FLOW WITH CACHING:

    Request 1:
    ├─ Call: service.getStudentsSimple()
    │ └─ Goes through PROXY
    ├─ Proxy checks cache: NOT FOUND
    ├─ Calls original method
    ├─ Executes: repo.findAll().map(mapper)
    ├─ Result stored in cache
    └─ Returns result


    Request 2 (same parameters, seconds later):
    ├─ Call: service.getStudentsSimple()
    │ └─ Goes through PROXY
    ├─ Proxy checks cache: FOUND!
    ├─ Original method NOT called
    ├─ Returns cached result immediately
    └─ No database query! (Fast!)


BEANPOSTPROCESSOR CREATES MANY PROXIES:

    @Cacheable      → CachingProxy
    @Transactional  → TransactionalProxy
    @Async          → AsyncProxy
    @Secured        → SecurityProxy
    
    A single bean can have MULTIPLE proxies layered!
    
    ┌─────────────────────────────┐
    │  Multiple Proxies (Layered)  │
    ├─────────────────────────────┤
    │  SecurityProxy              │
    │  └─ Checks authorization    │
    │     ↓                        │
    │  CachingProxy               │
    │  └─ Checks cache            │
    │     ↓                        │
    │  TransactionalProxy         │
    │  └─ Manages transactions    │
    │     ↓                        │
    │  ORIGINAL StudentService    │
    └─────────────────────────────┘
```

---

## Comparison: Different Initialization Methods

```
TIMELINE COMPARISON:

                Constructor          @PostConstruct    InitializingBean
                    │                    │                   │
    ┌───────────────────────────────────────────────────────────────┐
    │ PHASE 1: Instantiation                                        │
    │ new StudentService() called                                   │
    │         ✓ EXECUTED                      ✗                   ✗  │
    │                                                                │
    │ PHASE 2: Dependency Injection                                 │
    │ Constructor params: (repo, mapper)                            │
    │         ✓ EXECUTED                      ✗                   ✗  │
    │                                                                │
    │ PHASE 3: Aware Interfaces                                     │
    │ setBeanName(), setApplicationContext()                        │
    │         ✗                               ✗                   ✗  │
    │         (not in constructor)                                  │
    │                                                                │
    │ PHASE 4: BeanPostProcessor.before()                           │
    │         ✗                               ✗                   ✗  │
    │                                                                │
    │ PHASE 5: INITIALIZATION                                       │
    │ Code runs here...                                             │
    │         ✗                       ✓ EXECUTED       ✓ EXECUTED   │
    │                                                                │
    │ PHASE 6: BeanPostProcessor.after()                            │
    │         ✗                               ✗                   ✗  │
    │         (proxy creation)                                      │
    │                                                                │
    │ PHASE 7: Bean Ready                                           │
    │         ✗                               ✓                   ✓  │
    │                                                                │
    └───────────────────────────────────────────────────────────────┘

KEY DIFFERENCES:

    Constructor:
    ├─ Called FIRST
    ├─ Dependencies NOT yet available
    ├─ Can't use instance fields
    ├─ Should be lightweight
    └─ Use for: Assigning fields only

    @PostConstruct:
    ├─ Called AFTER dependency injection
    ├─ Dependencies fully available
    ├─ Modern Spring approach
    ├─ Can do heavy work (DB, network)
    └─ Use for: Initialize, load data, setup

    InitializingBean:
    ├─ Called AFTER @PostConstruct
    ├─ Legacy (from Spring 1.x)
    ├─ Implement interface required
    ├─ Less readable than annotation
    └─ Use for: Legacy projects only

    Destruction counterparts:
    ├─ Constructor → No equivalent
    ├─ @PostConstruct → @PreDestroy
    ├─ InitializingBean → DisposableBean
    └─ init-method → destroy-method (XML)
```

---

## Thread Safety in Singleton Beans

```
                     UNSAFE EXAMPLE
                     
    ┌─────────────────────────────────────┐
    │  StudentService (Singleton)         │
    │                                     │
    │  private List<String> userIds;      │ ← SHARED!
    │                                     │
    │  public void processUser(User u) {  │
    │    userIds = new ArrayList<>();     │ ← OVERWRITES!
    │    userIds.add(u.getId());          │
    │    // Use userIds...                │
    │  }                                  │
    └─────────────────────────────────────┘


    Thread 1 (Request A)    |  Thread 2 (Request B)
    ──────────────────────────────────────────────
    processUser(A) called   |
    userIds = []            |
    userIds.add("A-123")    |  processUser(B) called
                            |  userIds = []  ← CLEARS A's data!
    Use userIds:            |  userIds.add("B-456")
    ["B-456"] ??? ← WRONG!  |
                            |  Use userIds:
                            |  ["B-456"] ✓
    
    Result: A got B's data (DATA CORRUPTION!)


                     SAFE EXAMPLE
                     
    ┌─────────────────────────────────────┐
    │  StudentService (Singleton)         │
    │                                     │
    │  public void processUser(User u) {  │
    │    List<String> userIds             │ ← LOCAL variable
    │      = new ArrayList<>();           │    (Stack, not heap)
    │    userIds.add(u.getId());          │
    │    // Use userIds...                │
    │  }                                  │
    └─────────────────────────────────────┘


    Thread 1 (Request A)    |  Thread 2 (Request B)
    ──────────────────────────────────────────────
    processUser(A) called   |
    userIds (stack) = []    |
    userIds.add("A-123")    |  processUser(B) called
                            |  userIds (stack) = []
    Use userIds:            |
    ["A-123"] ✓             |  userIds.add("B-456")
                            |
                            |  Use userIds:
                            |  ["B-456"] ✓
    
    Result: Each thread has its own copy (SAFE!)


    RULE:
    ════════════════════════════════════════════════════
    ✗ UNSAFE: Store request data in instance fields
    ✓ SAFE:   Use only local variables or parameters
    ✓ SAFE:   Use ThreadLocal for shared data
    ✓ SAFE:   Use request-scoped beans
    ════════════════════════════════════════════════════
```

