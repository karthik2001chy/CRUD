package com.students.crud.lifecycle;

import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.Services.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BEAN LIFECYCLE DURING API REQUEST - Practical Example
 *
 * This controller demonstrates how beans are used during a real HTTP request.
 *
 *
 * STARTUP PHASE (Before any requests):
 * ────────────────────────────────────────────────────
 *
 * [1] Spring starts application
 * [2] Component scan finds @RestController
 * [3] BeanDefinition created for StudentLifecycleController
 * [4] StudentService bean created (dependency of controller)
 * [5] StudentRepository bean created (dependency of service)
 * [6] StudentMapper bean created (dependency of service)
 * [7] Controller created with injected service
 * [8] @PostConstruct methods called
 * [9] BeanPostProcessor proxies created
 * [10] Application ready to accept requests
 * [11] Server listening on port 8080
 *
 *
 * REQUEST HANDLING PHASE (Each HTTP request):
 * ────────────────────────────────────────────────────
 *
 * GET /api/lifecycle/students?page=0&size=20
 *
 * [1] HTTP Request arrives at Spring
 * [2] DispatcherServlet intercepts request
 * [3] Maps to: StudentLifecycleController.getStudentsWithLogging()
 * [4] Gets controller bean from container (SINGLETON - same instance!)
 * [5] Calls method: controller.getStudentsWithLogging(pageable)
 * [6] Controller calls service: this.studentService.getStudents(pageable)
 * [7] Service calls repository: this.studentRepository.findAll(pageable)
 * [8] Repository executes SQL query: SELECT * FROM students LIMIT 20
 * [9] Database returns data
 * [10] Repository returns Page<Student>
 * [11] Service returns to controller
 * [12] Controller returns ResponseEntity
 * [13] DispatcherServlet converts to JSON
 * [14] HTTP Response sent to client
 *
 *
 * KEY INSIGHT: SINGLETON PATTERN
 * ────────────────────────────────────────────────────
 *
 * Request 1 (from user A):
 * ├─ Uses StudentLifecycleController instance #1
 * ├─ Uses StudentService instance #1
 * └─ Uses StudentRepository proxy #1
 *
 * Request 2 (from user B at same time):
 * ├─ Uses SAME StudentLifecycleController instance #1
 * ├─ Uses SAME StudentService instance #1
 * └─ Uses SAME StudentRepository proxy #1
 *
 * Request 3 (from user C 1 hour later):
 * ├─ Uses SAME StudentLifecycleController instance #1
 * ├─ Uses SAME StudentService instance #1
 * └─ Uses SAME StudentRepository proxy #1
 *
 * Result: ONE bean instance serves MILLIONS of requests!
 *
 *
 * THREAD SAFETY IMPLICATION:
 * ────────────────────────────────────────────────────
 *
 * ✓ SAFE: Store data in local variables (stack)
 * public void getStudents(Pageable page) {
 *     List<Student> students = repository.findAll();  ← Local variable
 *     return students;  ← Each thread gets own copy
 * }
 *
 * ✗ UNSAFE: Store data in instance fields
 * public class StudentController {
 *     private List<Student> students;  ← SHARED by all threads!
 *
 *     public void getStudents() {
 *         students = repository.findAll();  ← Multiple threads modify!
 *         return students;  ← Data corruption possible
 *     }
 * }
 *
 *
 * ACTUAL EXECUTION TIMELINE:
 * ────────────────────────────────────────────────────
 *
 * STARTUP:
 * ┌─────────────────────────────────────────────────────┐
 * │ 1. CrudApplication.main() runs                      │
 * │ 2. SpringApplication.run() called                   │
 * │ 3. Component scan finds this controller             │
 * │ 4. StudentLifecycleController bean created          │
 * │ 5.   ├─ Constructor called                          │
 * │ 6.   ├─ StudentService injected                     │
 * │ 7.   │   ├─ StudentRepository injected              │
 * │ 8.   │   ├─ StudentMapper injected                  │
 * │ 9.   │   └─ @PostConstruct called                   │
 * │ 10.  ├─ Aware interfaces called                     │
 * │ 11.  ├─ @PostConstruct of controller called         │
 * │ 12.  └─ Proxies created                             │
 * │ 13. Application ready!                              │
 * └─────────────────────────────────────────────────────┘
 *               ↓
 * REQUEST HANDLING:
 * ┌─────────────────────────────────────────────────────┐
 * │ 14. HTTP GET /api/lifecycle/students arrives        │
 * │ 15. DispatcherServlet receives request              │
 * │ 16. Maps to: getStudentsWithLogging()               │
 * │ 17. Gets controller bean from container             │
 * │ 18. Calls: controller.getStudentsWithLogging()      │
 * │ 19.   ├─ Logs request                               │
 * │ 20.   ├─ Gets service from field (already injected) │
 * │ 21.   ├─ Calls: service.getStudents(pageable)       │
 * │ 22.   │   ├─ Service method runs                    │
 * │ 23.   │   ├─ Calls: repository.findAll(pageable)    │
 * │ 24.   │   │   └─ SQL executed: SELECT * LIMIT 20   │
 * │ 25.   │   └─ Returns Page<Student>                  │
 * │ 26.   └─ Returns ResponseEntity with page           │
 * │ 27. DispatcherServlet converts to JSON              │
 * │ 28. HTTP Response sent to client                    │
 * └─────────────────────────────────────────────────────┘
 *               ↓
 * SHUTDOWN:
 * ┌─────────────────────────────────────────────────────┐
 * │ 29. Kill signal received (Ctrl+C)                   │
 * │ 30. Stop accepting new requests                     │
 * │ 31. Wait for in-flight requests to complete         │
 * │ 32. Call @PreDestroy on controller                  │
 * │ 33. Call @PreDestroy on service                     │
 * │ 34. Close database connections                      │
 * │ 35. JVM shuts down                                  │
 * └─────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/lifecycle")
@CrossOrigin(origins = "*")
public class StudentLifecycleController {

    // Injected in constructor (SINGLETON - same instance for all requests)
    private final StudentService studentService;

    private static int requestCount = 0;

    /**
     * CONSTRUCTOR INJECTION
     *
     * Spring automatically calls this constructor during bean creation
     * and passes the StudentService bean as a parameter
     */
    public StudentLifecycleController(StudentService studentService) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("CONTROLLER INSTANTIATION");
        System.out.println("═".repeat(70));
        System.out.println("✓ StudentLifecycleController.constructor() called");
        System.out.println("  ├─ Received StudentService: " +
            studentService.getClass().getSimpleName());
        System.out.println("  └─ StudentService stored in field");

        this.studentService = studentService;
    }

    /**
     * DEMONSTRATION ENDPOINT - Shows full lifecycle
     *
     * GET /api/lifecycle/students?page=0&size=20
     *
     * curl "http://localhost:8080/api/lifecycle/students?page=0&size=20"
     */
    @GetMapping("/students")
    public ResponseEntity<Page<com.students.crud.DAO.Student>> getStudentsWithLogging(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {

        requestCount++;
        System.out.println("\n" + "═".repeat(70));
        System.out.println("API REQUEST #" + requestCount + ": GET /api/lifecycle/students");
        System.out.println("═".repeat(70));

        System.out.println("\n[CONTROLLER] getStudentsWithLogging() method called");
        System.out.println("  ├─ this.studentService = " +
            (studentService != null ? "INJECTED (singleton)" : "NULL (ERROR!)"));
        System.out.println("  ├─ Page: " + pageable.getPageNumber());
        System.out.println("  └─ Size: " + pageable.getPageSize());

        System.out.println("\n[CONTROLLER → SERVICE] Calling: studentService.getStudents()");

        // Call the service - this.studentService is the same instance for all requests!
        Page<com.students.crud.DAO.Student> students = studentService.getStudents(pageable);

        System.out.println("\n[SERVICE → CONTROLLER] Returned " + students.getTotalElements() +
            " students");
        System.out.println("  └─ Creating ResponseEntity with data");

        return ResponseEntity.ok(students);
    }

    /**
     * DEMONSTRATION ENDPOINT - Shows search with caching
     *
     * GET /api/lifecycle/students/search?name=John
     */
    @GetMapping("/students/search")
    public ResponseEntity<List<StudentSimpleDto>> searchStudents(
            @RequestParam(name = "name") String name) {

        requestCount++;
        System.out.println("\n" + "═".repeat(70));
        System.out.println("API REQUEST #" + requestCount + ": GET /api/lifecycle/students/search");
        System.out.println("═".repeat(70));

        System.out.println("\n[CONTROLLER] searchStudents() method called");
        System.out.println("  ├─ Search name: " + name);
        System.out.println("  ├─ this.studentService = INJECTED (singleton)");
        System.out.println("  └─ Calling service...");

        // This might use caching (BeanPostProcessor proxy)
        List<StudentSimpleDto> results = studentService.searchStudentsByNameSimple(name);

        System.out.println("\n[SERVICE → CONTROLLER] Returned " + results.size() + " results");
        System.out.println("  ├─ Caching might have been used");
        System.out.println("  └─ Creating ResponseEntity with data");

        return ResponseEntity.ok(results);
    }

    /**
     * DEMONSTRATION ENDPOINT - Shows singleton nature
     *
     * Call this multiple times to prove same bean instance is used!
     * GET /api/lifecycle/info
     */
    @GetMapping("/info")
    public ResponseEntity<String> getBeanInfo() {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("BEAN SINGLETON DEMONSTRATION");
        System.out.println("─".repeat(70));
        System.out.println("Service bean address: " + System.identityHashCode(studentService));
        System.out.println("(Same address = same instance for all requests)");
        System.out.println("Total requests handled: " + requestCount);
        System.out.println("─".repeat(70));

        String info = "Service bean hash: " + System.identityHashCode(studentService) +
                     ", Request #: " + requestCount;
        return ResponseEntity.ok(info);
    }

    /**
     * EXPLANATION METHOD - Call this to understand the lifecycle
     */
    public void explainControllerLifecycle() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("CONTROLLER BEAN LIFECYCLE IN API REQUESTS");
        System.out.println("═".repeat(70));

        System.out.println("""
        
        STARTUP PHASE:
        ┌────────────────────────────────────────────────┐
        │ 1. Component scan finds @RestController        │
        │ 2. BeanDefinition created                      │
        │ 3. StudentService bean created first           │
        │    ├─ StudentRepository injected               │
        │    ├─ StudentMapper injected                   │
        │    └─ @PostConstruct called                    │
        │ 4. StudentLifecycleController bean created     │
        │    ├─ Constructor called                       │
        │    ├─ StudentService injected                  │
        │    ├─ @PostConstruct called                    │
        │    └─ Proxies created                          │
        │ 5. Application ready on port 8080              │
        └────────────────────────────────────────────────┘
        
        REQUEST HANDLING (Multiple requests):
        ┌────────────────────────────────────────────────┐
        │ Request 1: GET /api/lifecycle/students         │
        │ ├─ Gets StudentLifecycleController bean        │
        │ ├─ Calls getStudentsWithLogging()              │
        │ ├─ Controller uses this.studentService         │
        │ └─ Response sent                               │
        │                                                │
        │ Request 2: GET /api/lifecycle/students/search  │
        │ ├─ Gets SAME StudentLifecycleController bean   │
        │ ├─ Calls searchStudents()                      │
        │ ├─ Controller uses SAME this.studentService    │
        │ └─ Response sent                               │
        │                                                │
        │ Request 3: GET /api/lifecycle/info             │
        │ ├─ Gets SAME StudentLifecycleController bean   │
        │ ├─ Calls getBeanInfo()                         │
        │ ├─ Controller uses SAME this.studentService    │
        │ └─ Response sent                               │
        │                                                │
        │ ALL THREE REQUESTS USE SAME BEAN INSTANCES!    │
        └────────────────────────────────────────────────┘
        
        SHUTDOWN PHASE:
        ┌────────────────────────────────────────────────┐
        │ 1. Kill signal received (Ctrl+C)               │
        │ 2. Stop accepting new requests                 │
        │ 3. Wait for in-flight requests to complete     │
        │ 4. @PreDestroy called on controller            │
        │ 5. @PreDestroy called on service               │
        │ 6. Database/Redis connections closed           │
        │ 7. JVM shut down                               │
        └────────────────────────────────────────────────┘
        
        KEY POINTS:
        ✓ Spring creates ONE instance of each @Component/@Service/@Controller
        ✓ Same instance used for ALL requests (singleton scope)
        ✓ thread-safe because Spring handles request-scoped data
        ✓ Injection happens at startup, not at request time
        ✓ Constructor called ONCE during application startup
        ✓ Methods called MANY TIMES (once per request)
        """);

        System.out.println("═".repeat(70) + "\n");
    }
}

