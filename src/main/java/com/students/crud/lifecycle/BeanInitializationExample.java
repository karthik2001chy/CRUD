package com.students.crud.lifecycle;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * INITIALIZATION PHASE - Shows all three initialization methods
 *
 * Spring provides THREE ways to initialize a bean:
 * 1. @PostConstruct annotation (PREFERRED - modern)
 * 2. InitializingBean interface (LEGACY)
 * 3. init-method in XML (LEGACY - not used in modern Spring)
 *
 *
 * EXECUTION ORDER for initialization:
 * ────────────────────────────────────────────────────
 *
 * [Before] BeanPostProcessor.postProcessBeforeInitialization()
 *     ↓
 * [1] @PostConstruct methods called (all of them, in order)
 *     ↓
 * [2] InitializingBean.afterPropertiesSet() called
 *     ↓
 * [3] init-method called (XML only)
 *     ↓
 * [After] BeanPostProcessor.postProcessAfterInitialization()
 *
 *
 * REAL EXAMPLE from your CRUD app:
 * ────────────────────────────────────────────────────
 *
 * StudentService has:
 *
 * @PostConstruct
 * public void init() {
 *     System.out.println("3. @PostConstruct called - initialization logic");
 *     this.cachedStudents = studentRepository.findAll();
 *     System.out.println("Loaded " + cachedStudents.size() + " students");
 * }
 *
 * This is called AFTER:
 * - Constructor finishes
 * - Dependencies injected
 * - BeanPostProcessor.before() called
 *
 *
 * WHY INITIALIZATION?
 * ────────────────────────────────────────────────────
 *
 * Sometimes you need to do something after bean is created.
 * You can't do it in constructor because:
 * - Dependencies not injected yet
 * - Spring not finished initializing
 * - Other beans not ready
 *
 * Examples:
 * 1. Load cache from database
 * 2. Connect to external services
 * 3. Start background threads
 * 4. Initialize monitoring/metrics
 * 5. Validate configuration
 * 6. Setup listeners
 *
 *
 * FROM STUDENT CACHE PERSPECTIVE:
 * ────────────────────────────────────────────────────
 *
 * @Service
 * public class StudentService {
 *     private final StudentRepository studentRepository;
 *     private final StudentMapper studentMapper;
 *     private List<Student> cachedStudents;  // Empty initially
 *
 *     public StudentService(StudentRepository studentRepository,
 *                           StudentMapper studentMapper) {
 *         this.studentRepository = studentRepository;
 *         this.studentMapper = studentMapper;
 *         System.out.println("Constructor: cachedStudents = null");
 *     }
 *
 *     @PostConstruct
 *     public void init() {
 *         System.out.println("@PostConstruct: Loading cache...");
 *         cachedStudents = studentRepository.findAll();  // Now loaded!
 *     }
 *
 *     public List<Student> getCachedStudents() {
 *         return cachedStudents;  // Returns loaded cache
 *     }
 * }
 *
 *
 * Execution:
 * ┌─────────────────────────┐
 * │ 1. Constructor called   │ → cachedStudents = null
 * │ 2. Dependencies injected│ → studentRepository available
 * │ 3. @PostConstruct init()│ → cachedStudents = [loaded students]
 * │ 4. Bean ready           │ → getCachedStudents() returns loaded list
 * └─────────────────────────┘
 */
@Component
public class BeanInitializationExample implements InitializingBean {

    private String resourceStatus = "NOT_INITIALIZED";
    private boolean cacheLoaded = false;

    public BeanInitializationExample() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("BEAN INITIALIZATION PHASE");
        System.out.println("═".repeat(70));
        System.out.println("\n[1] Constructor called");
        System.out.println("    resourceStatus = " + color("RED", "NOT_INITIALIZED"));
        System.out.println("    cacheLoaded = " + color("RED", "false"));
        System.out.println("    ├─ Dependencies NOT YET injected");
        System.out.println("    └─ NOT ready to use yet");
    }

    /**
     * Method 1: @PostConstruct (PREFERRED)
     *
     * Called AFTER:
     * - Constructor
     * - Dependency injection
     * - Aware interfaces
     *
     * Called BEFORE:
     * - BeanPostProcessor.postProcessAfterInitialization()
     * - Bean is used
     */
    @PostConstruct
    public void initializeResources() {
        System.out.println("\n[2] @PostConstruct - initializeResources() called");
        System.out.println("    ├─ All dependencies now available");
        System.out.println("    ├─ Safe to initialize resources");
        System.out.println("    └─ Performing initialization...");

        // Simulate loading cache from database
        System.out.println("\n    Loading student cache from database...");
        try {
            Thread.sleep(100);  // Simulate DB call
            resourceStatus = "INITIALIZED";
            cacheLoaded = true;

            System.out.println("    ✓ Cache loaded successfully");
            System.out.println("    resourceStatus = " + color("GREEN", "INITIALIZED"));
            System.out.println("    cacheLoaded = " + color("GREEN", "true"));
        } catch (InterruptedException e) {
            System.out.println("    ✗ Cache loading failed");
        }
    }

    /**
     * Method 2: InitializingBean.afterPropertiesSet() (LEGACY)
     *
     * Called AFTER @PostConstruct
     *
     * Modern Spring apps use @PostConstruct instead
     * InitializingBean is for backward compatibility
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("\n[3] InitializingBean.afterPropertiesSet() called");
        System.out.println("    ├─ Alternative to @PostConstruct (legacy)");
        System.out.println("    ├─ Deprecated - use @PostConstruct instead");
        System.out.println("    ├─ Current status: resourceStatus = " +
            color("GREEN", resourceStatus));
        System.out.println("    └─ Current status: cacheLoaded = " +
            color("GREEN", String.valueOf(cacheLoaded)));
    }

    /**
     * Method 3: init-method in XML (LEGACY - Not used here)
     *
     * In XML:
     * <bean id="bean" class="MyClass" init-method="initialize"/>
     *
     * Then in class:
     * public void initialize() {
     *     // Called after afterPropertiesSet()
     * }
     *
     * Modern Spring Boot does NOT use XML, so this is rarely seen
     */

    // ==================== METHODS SHOWING STATE ====================

    public void demonstrateInitialization() {
        System.out.println("\n[4] Bean initialization complete");
        System.out.println("    ├─ resourceStatus = " + color("GREEN", resourceStatus));
        System.out.println("    ├─ cacheLoaded = " + color("GREEN", String.valueOf(cacheLoaded)));
        System.out.println("    ├─ Bean ready to serve requests");
        System.out.println("    └─ getResourceStatus() = " + getResourceStatus());
    }

    /**
     * This can be called safely because initialization is complete
     */
    public String getResourceStatus() {
        if (!cacheLoaded) {
            throw new RuntimeException("Bean not initialized!");
        }
        return resourceStatus;
    }

    /**
     * This can be called safely because cache is loaded
     */
    public boolean isCacheLoaded() {
        return cacheLoaded;
    }

    // ==================== COMPARISON WITH CONSTRUCTOR ====================

    public void explainWhyPostConstructIsNeeded() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("WHY @PostConstruct?");
        System.out.println("═".repeat(70));

        System.out.println("\nPROBLEM: Using constructor for initialization");
        System.out.println("─".repeat(70));
        System.out.println("""
        @Service
        public class StudentService {
            private final StudentRepository repository;
            private List<Student> cache;
            
            public StudentService(StudentRepository repository) {
                this.repository = repository;
                
                // CAN'T do this here!
                // this.cache = repository.findAll();  ← Wrong!
                
                // Why? Spring Boot might:
                // - Wrap this bean in a proxy
                // - Apply proxies for @Transactional, @Cacheable
                // - Modify the class
                // - Dependencies might not be fully ready
            }
        }
        """);

        System.out.println("\nSOLUTION: Use @PostConstruct");
        System.out.println("─".repeat(70));
        System.out.println("""
        @Service
        public class StudentService {
            private final StudentRepository repository;
            private List<Student> cache;
            
            public StudentService(StudentRepository repository) {
                this.repository = repository;
            }
            
            @PostConstruct
            public void init() {
                // NOW it's safe!
                this.cache = repository.findAll();  ← Correct!
                
                // At this point:
                // - Constructor finished
                // - Dependencies injected
                // - Proxies created
                // - All other beans initialized
                // - Everything ready
            }
        }
        """);

        System.out.println("\nCOMPARISON:");
        System.out.println("─".repeat(70));
        System.out.println("Constructor        @PostConstruct");
        System.out.println("─".repeat(35) + " ─".repeat(17));
        System.out.println("✗ Dependencies     ✓ Dependencies");
        System.out.println("  not injected yet    fully available");
        System.out.println("");
        System.out.println("✗ Other beans      ✓ Other beans");
        System.out.println("  may not exist       exist and ready");
        System.out.println("");
        System.out.println("✗ Proxies not      ✓ Proxies");
        System.out.println("  created yet         already created");
        System.out.println("");
        System.out.println("✗ Spring not       ✓ Spring fully");
        System.out.println("  fully ready         ready");
        System.out.println("");
        System.out.println("✓ Lightweight      ✗ Can be slow");
        System.out.println("  (no I/O)           (DB, network calls)");

        System.out.println("\n" + "═".repeat(70) + "\n");
    }

    // ==================== HELPER ====================

    private static String color(String colorName, String text) {
        return switch(colorName) {
            case "RED" -> "\033[91m" + text + "\033[0m";
            case "GREEN" -> "\033[92m" + text + "\033[0m";
            case "YELLOW" -> "\033[93m" + text + "\033[0m";
            default -> text;
        };
    }
}

