# Spring Bean Lifecycle - 7-Day Study Plan

## Day 1: Fundamentals (1-2 hours)

### Morning (30 min)
- [ ] Read README.md - Get overview
- [ ] Read QUICK_REFERENCE.md - 5-minute summary
- [ ] Understand 3 key concepts:
  1. Spring Container manages beans
  2. Beans created at startup
  3. One instance (singleton) serves all requests

### Afternoon (1-1.5 hours)
- [ ] Read Phase 1-2 in SpringBeanLifecycleExplanation.md
- [ ] Understand:
  - Component scanning (@Component, @Service, @Repository)
  - Bean instantiation order
  - Dependency graph

### Action
```bash
cd /Users/karthik/IdeaProjects/SpringBean
mvn spring-boot:run
# Watch console for BeanLifecycleDemo logs
# Press Ctrl+C after seeing initialization complete
```

---

## Day 2: Dependency Injection (1-2 hours)

### Morning (45 min)
- [ ] Read Phase 3 in SpringBeanLifecycleExplanation.md
- [ ] Read StudentServiceWithLogging.java completely
- [ ] Understand constructor injection:
  - Why it's best practice
  - How Spring resolves dependencies
  - Parameter matching

### Afternoon (1-1.5 hours)
- [ ] Read StudentService.java and StudentController.java from your CRUD app
- [ ] Trace dependency path:
  - StudentController → StudentService → StudentRepository
- [ ] Answer question: "How many StudentRepository instances?"
- [ ] Call demonstrateDependencyInjection() from StudentServiceWithLogging

### Practice
- [ ] Try to explain constructor injection to someone
- [ ] Draw dependency graph for your CRUD app
- [ ] Identify if any field injection used (shouldn't be!)

---

## Day 3: Awareness & Proxying (1-2 hours)

### Morning (45 min)
- [ ] Read Phase 4 in SpringBeanLifecycleExplanation.md
- [ ] Read Phase 5 (BeanPostProcessor)
- [ ] Read CustomBeanPostProcessor.java
- [ ] Understand:
  - BeanNameAware, ApplicationContextAware
  - What BeanPostProcessor does
  - How proxies are created

### Afternoon (1-1.5 hours)
- [ ] Read how caching proxy works (in VISUAL_DIAGRAMS.md)
- [ ] Find @Cacheable in StudentService
- [ ] Understand the flow:
  - Method is wrapped in proxy
  - Cache checked first
  - Original method called only if miss
- [ ] Trace proxy creation in CustomBeanPostProcessor.java

### Practice
- [ ] Explain to someone: "How does @Cacheable work?"
- [ ] Draw caching proxy creation diagram
- [ ] Identify what happens in postProcessAfterInitialization()

---

## Day 4: Initialization (1-2 hours)

### Morning (45 min)
- [ ] Read Phase 6 in SpringBeanLifecycleExplanation.md
- [ ] Read BeanInitializationExample.java
- [ ] Understand:
  - @PostConstruct timing
  - InitializingBean vs @PostConstruct
  - Why NOT constructor

### Afternoon (1-1.5 hours)
- [ ] Run application and watch initialization logs
- [ ] Find @PostConstruct in StudentService
- [ ] Call explainWhyPostConstructIsNeeded()
- [ ] Think: "What would break if I load cache in constructor?"

### Practice
- [ ] Answer Q7 from INTERVIEW_QUESTIONS.md
- [ ] Explain: "@PostConstruct is called AFTER constructor and injection"
- [ ] Identify 3 things you'd do in @PostConstruct

---

## Day 5: API Requests & Destruction (1-2 hours)

### Morning (45 min)
- [ ] Read Phase 7 in SpringBeanLifecycleExplanation.md
- [ ] Read StudentLifecycleController.java
- [ ] Understand request flow:
  - Request arrives → Controller bean retrieved
  - Service bean injected → Repository bean injected
  - Singleton means SAME instance for all requests

### Afternoon (1-1.5 hours)
- [ ] Start application
- [ ] Call API endpoints multiple times:
  ```bash
  curl http://localhost:8080/api/lifecycle/info
  # Note the bean hash - same for multiple calls!
  
  curl http://localhost:8080/api/lifecycle/students?page=0&size=20
  # See full request flow
  ```
- [ ] Read Phase 8 (Destruction)
- [ ] Press Ctrl+C and watch @PreDestroy logs

### Practice
- [ ] Trace full request: HTTP → Controller → Service → Repository → DB
- [ ] Draw request flow diagram
- [ ] Understand why singleton beans need thread safety

---

## Day 6: Deep Dive & Interviews (2-3 hours)

### Morning (1 hour)
- [ ] Read VISUAL_DIAGRAMS.md completely
- [ ] Study each diagram:
  - Complete lifecycle flowchart
  - HTTP request processing
  - Singleton vs prototype
  - Destruction timeline
  - Caching proxy

### Afternoon (1.5-2 hours)
- [ ] Read INTERVIEW_QUESTIONS.md
- [ ] Start with Easy level (Q1-5)
  - Answer before reading answers
  - Verify your understanding
- [ ] Move to Medium level (Q6-10)
- [ ] Review Hard level (Q11-18)
- [ ] Try scenario questions (Q19-20)

### Practice
- [ ] Prepare 2-minute answer for: "Explain Spring Bean Lifecycle"
- [ ] Answer Q16 (circular dependencies) in detail
- [ ] Answer Q17 (thread safety) with examples

---

## Day 7: Mastery & Practice (2-3 hours)

### Morning (1 hour)
- [ ] Revisit QUICK_REFERENCE.md - should understand everything
- [ ] Review SpringBeanLifecycleExplanation.md - take notes on gaps
- [ ] Create personal cheat sheet with:
  - Execution order
  - Key phases
  - Common mistakes

### Afternoon (1.5-2 hours)
- [ ] Mock interview with yourself:
  - Explain complete bean lifecycle (5 minutes)
  - Answer 5 random interview questions
  - Draw diagrams from memory
  
- [ ] Test your understanding:
  - [ ] Q: "When is @PostConstruct called?" → Explain
  - [ ] Q: "How many StudentService instances?" → Answer + explain
  - [ ] Q: "How does @Cacheable work?" → Draw diagram
  - [ ] Q: "Why constructor injection?" → List 5 reasons
  - [ ] Q: "Thread safety in singleton?" → Give example

### Practice
- [ ] Teach someone about Spring Bean Lifecycle
- [ ] Review any remaining unclear concepts
- [ ] Write down 3 key takeaways

---

## Study Materials Checklist

### Markdown Guides
- [ ] README.md (overview)
- [ ] QUICK_REFERENCE.md (cheat sheet)
- [ ] SpringBeanLifecycleExplanation.md (main guide)
- [ ] INTERVIEW_QUESTIONS.md (Q&A)
- [ ] VISUAL_DIAGRAMS.md (diagrams)

### Java Code Files
- [ ] BeanLifecycleDemo.java (all hooks)
- [ ] StudentServiceWithLogging.java (DI)
- [ ] BeanInitializationExample.java (initialization)
- [ ] CustomBeanPostProcessor.java (proxies)
- [ ] StudentLifecycleController.java (REST API)

### Your CRUD App Files
- [ ] CrudApplication.java (@SpringBootApplication, @EnableCaching)
- [ ] StudentService.java (@Service, @PostConstruct, @Cacheable)
- [ ] StudentController.java (@RestController)
- [ ] StudentRepository.java (@Repository)
- [ ] StudentMapper.java (@Component)
- [ ] RedisConfig.java (@Configuration)

---

## Key Concepts to Master

### Must Know (Critical)
- [ ] 8 phases of bean lifecycle
- [ ] Instantiation order (dependency order)
- [ ] Constructor injection (best practice)
- [ ] @PostConstruct timing (after injection, before use)
- [ ] Singleton scope (one instance, all requests)
- [ ] @PreDestroy (on shutdown)

### Should Know (Important)
- [ ] BeanPostProcessor and proxy creation
- [ ] @Cacheable, @Transactional, @Async proxying
- [ ] Aware interfaces (BeanNameAware, ApplicationContextAware)
- [ ] InitializingBean vs @PostConstruct
- [ ] DisposableBean vs @PreDestroy
- [ ] Thread safety in singletons

### Nice to Know (Good to have)
- [ ] Circular dependency detection
- [ ] Multiple scopes (prototype, request)
- [ ] Bean lifecycle customization
- [ ] Custom BeanPostProcessor implementation
- [ ] Performance implications

---

## Interview Preparation

### 30-Second Explanation
"Spring creates beans at startup using component scanning. Dependencies are injected via constructors. BeanPostProcessor creates proxies for annotations like @Cacheable. Beans are initialized with @PostConstruct, ready for requests. Singleton beans are reused for all requests. On shutdown, @PreDestroy is called for cleanup."

### 2-Minute Explanation
See SpringBeanLifecycleExplanation.md - "Complete Bean Lifecycle Timeline" section

### 5-Minute Explanation
Use VISUAL_DIAGRAMS.md - "Complete Bean Lifecycle Flow Chart"

---

## Practice Questions

### Easy (Should answer in 30 seconds)
1. When are beans created?
2. How many StudentService instances?
3. What does @PostConstruct do?
4. What's better: constructor or field injection?
5. When is @PreDestroy called?

### Medium (Should answer in 1-2 minutes)
1. Explain the complete bean lifecycle
2. How does @Cacheable work?
3. What's a BeanPostProcessor?
4. Explain dependency injection
5. What are Aware interfaces?

### Hard (Should answer in 2-3 minutes)
1. How does Spring handle circular dependencies?
2. Why is singleton and thread safety important?
3. Explain proxy creation in detail
4. How do @Transactional proxies work?
5. What happens in a multi-threaded environment?

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Initializing in Constructor
```java
@Service
public class StudentService {
    private final StudentRepository repository;
    private List<Student> cache;
    
    public StudentService(StudentRepository repository) {
        this.repository = repository;
        this.cache = repository.findAll();  // WRONG!
        // Proxies not created yet!
    }
}
```

✅ **Correct:**
```java
@PostConstruct
public void init() {
    this.cache = repository.findAll();  // RIGHT!
}
```

### ❌ Mistake 2: Field Injection
```java
@Service
public class StudentService {
    @Autowired  // NOT recommended
    private StudentRepository repository;
}
```

✅ **Correct:**
```java
@Service
public class StudentService {
    private final StudentRepository repository;  // Final!
    
    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
}
```

### ❌ Mistake 3: Storing Request Data in Instance Fields
```java
@Service
public class StudentService {  // SINGLETON
    private String userId;  // SHARED by all threads!
    
    public void processStudent(String id) {
        this.userId = id;  // WRONG! Overwrite others' data
    }
}
```

✅ **Correct:**
```java
@Service
public class StudentService {
    public void processStudent(String id) {
        String userId = id;  // Local variable - thread-safe
    }
}
```

---

## Success Criteria

### After Day 1
- [ ] Understand component scanning
- [ ] Know beans are created at startup
- [ ] Grasp singleton concept

### After Day 2
- [ ] Explain constructor injection
- [ ] Understand dependency resolution
- [ ] Know instantiation order

### After Day 3
- [ ] Explain BeanPostProcessor
- [ ] Understand @Cacheable proxying
- [ ] Know Aware interfaces

### After Day 4
- [ ] Know when @PostConstruct is called
- [ ] Understand why NOT in constructor
- [ ] Know InitializingBean vs @PostConstruct

### After Day 5
- [ ] Trace full request flow
- [ ] Understand singleton implications
- [ ] Know when @PreDestroy is called

### After Day 6
- [ ] Answer easy interview questions
- [ ] Answer medium questions
- [ ] Understand hard scenarios

### After Day 7
- [ ] Give 5-minute explanation
- [ ] Answer all interview questions
- [ ] Teach someone else

---

## Resources Summary

| Resource | Type | Time | Best For |
|----------|------|------|----------|
| README.md | Guide | 10 min | Overview |
| QUICK_REFERENCE.md | Cheat sheet | 5 min | Fast lookup |
| SpringBeanLifecycleExplanation.md | Full guide | 30 min | Deep understanding |
| INTERVIEW_QUESTIONS.md | Q&A | 30 min | Interview prep |
| VISUAL_DIAGRAMS.md | Diagrams | 15 min | Visual learning |
| BeanLifecycleDemo.java | Code | 10 min | Execution order |
| StudentServiceWithLogging.java | Code | 10 min | Dependency injection |
| BeanInitializationExample.java | Code | 10 min | Initialization |
| CustomBeanPostProcessor.java | Code | 10 min | Proxy creation |
| StudentLifecycleController.java | Code | 10 min | Real-world usage |

---

## Final Tips

1. **Understand the ORDER** - Lifecycle happens in strict sequence
2. **Use your CRUD app** - All examples use your actual code
3. **Run the code** - See logs and understand timing
4. **Draw diagrams** - Visualizing helps memory
5. **Teach others** - Best way to solidify understanding
6. **Review regularly** - Spaced repetition helps retention
7. **Answer questions** - Test yourself frequently
8. **Connect concepts** - Understand how parts relate

---

## You've Got This! 🚀

After 7 days of study using this plan, you will:

✅ Understand Spring Bean Lifecycle completely
✅ Be able to explain it in interviews
✅ Know best practices (constructor injection, etc.)
✅ Understand @PostConstruct, @PreDestroy, @Cacheable
✅ Be confident in Spring interviews
✅ Write better Spring code

Good luck! You're going to master this! 🎉

