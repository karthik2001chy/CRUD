package com.students.crud.lifecycle;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.mapper.StudentMapper;
import com.students.crud.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DEPENDENCY INJECTION EXAMPLE - Shows how Spring injects dependencies
 *
 * Constructor Injection (BEST PRACTICE):
 * ────────────────────────────────────────────────────
 *
 * When Spring creates StudentService:
 *
 * 1. Looks at the constructor parameters:
 *    public StudentServiceWithLogging(
 *        StudentRepository studentRepository,  ← Type 1
 *        StudentMapper studentMapper           ← Type 2
 *    )
 *
 * 2. Checks if each type is available as a bean:
 *    ✓ Is there a StudentRepository bean?  YES (from @Repository)
 *    ✓ Is there a StudentMapper bean?      YES (from @Component)
 *
 * 3. Gets both beans from the container:
 *    StudentRepository repo = container.getBean(StudentRepository.class);
 *    StudentMapper mapper = container.getBean(StudentMapper.class);
 *
 * 4. Calls the constructor with beans:
 *    StudentServiceWithLogging service = new StudentServiceWithLogging(repo, mapper);
 *
 * 5. Result:
 *    - studentRepository field = repo
 *    - studentMapper field = mapper
 *    - Both are final (immutable)
 *    - If dependency missing → Startup fails (GOOD!)
 *
 *
 * ACTUAL EXECUTION TIMELINE:
 * ────────────────────────────────────────────────────
 *
 * [1] Spring starts
 * [2] Component scan finds @Service on StudentServiceWithLogging
 * [3] Registers BeanDefinition (not created yet)
 * [4] Component scan finds @Repository on StudentRepository interface
 * [5] Component scan finds @Component on StudentMapper
 * [6] Creates StudentRepository bean first (no dependencies)
 * [7] Creates StudentMapper bean (no dependencies)
 * [8] Creates StudentServiceWithLogging bean:
 *     - Sees constructor params: StudentRepository, StudentMapper
 *     - Gets StudentRepository from container
 *     - Gets StudentMapper from container
 *     - Calls: new StudentServiceWithLogging(repo, mapper)
 *     - Constructor logs: "StudentServiceWithLogging.constructor() called"
 *     - this.studentRepository = repo
 *     - this.studentMapper = mapper
 * [9] @PostConstruct method called
 *     - Bean is now fully initialized
 *     - All dependencies available
 *     - Can access fields safely
 * [10] Bean ready to serve requests
 *
 *
 * DURING API REQUEST:
 * ────────────────────────────────────────────────────
 *
 * GET /api/students?page=0&size=20
 *         ↓
 * StudentController.listStudents() called
 *         ↓
 * studentService.getStudents(pageable) called
 * (this.studentService = already created bean from container)
 *         ↓
 * this.studentRepository.findAll(pageable) called
 * (this.studentRepository = bean injected in constructor)
 *         ↓
 * SELECT * FROM students LIMIT 20 OFFSET 0
 *         ↓
 * Page<Student> returned
 *         ↓
 * Response sent to client
 *
 *
 * COMPARISON WITH FIELD INJECTION (NOT RECOMMENDED):
 * ────────────────────────────────────────────────────
 *
 * @Service
 * public class StudentServiceBad {
 *     @Autowired  ← Spring sees this annotation
 *     private StudentRepository studentRepository;
 *
 *     @Autowired
 *     private StudentMapper studentMapper;
 *
 *     // Constructor - Spring doesn't inject here
 *     public StudentServiceBad() {
 *         // studentRepository = null here!
 *         // studentMapper = null here!
 *     }
 * }
 *
 * Why is this bad?
 * 1. Can't use fields in constructor
 * 2. Fields can be modified after creation (not final)
 * 3. Hard to test (need Spring to inject)
 * 4. NullPointerException risk
 * 5. Dependency not explicit in method signature
 */
@Service
public class StudentServiceWithLogging {

    // These are injected via constructor (FINAL = immutable)
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    private boolean isInitialized = false;

    /**
     * CONSTRUCTOR - Dependencies injected here
     *
     * Spring calls this constructor automatically with beans from container
     *
     * @param studentRepository bean from container (@Repository)
     * @param studentMapper bean from container (@Component)
     */
    public StudentServiceWithLogging(
            StudentRepository studentRepository,
            StudentMapper studentMapper) {

        // Log dependency injection
        System.out.println("\n" + "═".repeat(70));
        System.out.println("DEPENDENCY INJECTION IN ACTION");
        System.out.println("═".repeat(70));
        System.out.println("\n✓ StudentServiceWithLogging.constructor() called");
        System.out.println("  ├─ Received StudentRepository: " +
            studentRepository.getClass().getSimpleName());
        System.out.println("  ├─ Received StudentMapper: " +
            studentMapper.getClass().getSimpleName());
        System.out.println("  └─ Both stored in final fields (immutable)");

        // Assign dependencies
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;

        System.out.println("\n  Dependencies are now available:");
        System.out.println("  ├─ this.studentRepository = NOT NULL ✓");
        System.out.println("  ├─ this.studentMapper = NOT NULL ✓");
        System.out.println("  └─ Ready to use in methods");
    }

    /**
     * POST-CONSTRUCTION - Called after all injections complete
     *
     * This is called AFTER the constructor finishes
     * All dependencies are guaranteed to be non-null here
     */
    @PostConstruct
    public void init() {
        System.out.println("\n✓ @PostConstruct - init() called");
        System.out.println("  ├─ All dependencies verified as non-null");
        System.out.println("  ├─ this.studentRepository = " +
            studentRepository.getClass().getSimpleName());
        System.out.println("  ├─ this.studentMapper = " +
            studentMapper.getClass().getSimpleName());
        System.out.println("  └─ Service is now initialized");

        isInitialized = true;
    }

    /**
     * EXAMPLE METHOD - Uses injected dependencies
     *
     * This method can safely use studentRepository and studentMapper
     * because they were injected in the constructor
     */
    public Page<Student> getStudents(Pageable pageable) {
        if (!isInitialized) {
            throw new RuntimeException("Service not initialized!");
        }

        System.out.println("\n" + "─".repeat(70));
        System.out.println("API REQUEST: GET /api/students");
        System.out.println("─".repeat(70));
        System.out.println("\n1. Controller called: StudentController.listStudents()");
        System.out.println("2. Controller called: this.studentService.getStudents()");
        System.out.println("3. Service: getStudents() executing...");
        System.out.println("   ├─ Using injected studentRepository");
        System.out.println("   └─ Calling: this.studentRepository.findAll(pageable)");

        // Use injected repository
        Page<Student> result = studentRepository.findAll(pageable);

        System.out.println("   ├─ Database returned Page with " +
            result.getTotalElements() + " students");
        System.out.println("   └─ Returning to Controller");

        return result;
    }

    /**
     * EXAMPLE METHOD - Uses multiple injected dependencies
     */
    public List<StudentSimpleDto> searchAndMapStudents(String name) {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("API REQUEST: GET /api/students/search/name?name=" + name);
        System.out.println("─".repeat(70));
        System.out.println("\n1. Controller called: StudentController.searchStudentsByNameSimple()");
        System.out.println("2. Service: searchAndMapStudents() executing...");

        // Use injected repository
        System.out.println("   ├─ Step 1: Getting all students from repository");
        List<Student> allStudents = studentRepository.findAll();

        // Filter
        System.out.println("   ├─ Step 2: Filtering by name: " + name);
        List<Student> filtered = allStudents.stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        // Use injected mapper
        System.out.println("   ├─ Step 3: Mapping " + filtered.size() +
            " students to DTOs using mapper");
        List<StudentSimpleDto> dtos = filtered.stream()
                .map(studentMapper::toSimpleDto)
                .collect(Collectors.toList());

        System.out.println("   └─ Returning " + dtos.size() + " DTOs to Controller");

        return dtos;
    }

    /**
     * CLEANUP - Called on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        System.out.println("\n✗ @PreDestroy - cleanup() called");
        System.out.println("  ├─ Application is shutting down");
        System.out.println("  ├─ Closing studentRepository");
        System.out.println("  ├─ Releasing studentMapper");
        System.out.println("  └─ Service destroyed");

        isInitialized = false;
    }

    // ==================== DEMONSTRATION METHOD ====================

    /**
     * Call this method to see the full dependency injection explanation
     */
    public void demonstrateDependencyInjection() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("DEPENDENCY INJECTION EXPLAINED");
        System.out.println("═".repeat(70));

        System.out.println("\n1. CONSTRUCTOR PHASE:");
        System.out.println("   When Spring saw this class, it did:");
        System.out.println("   - Constructor has 2 parameters");
        System.out.println("   - Parameter 1: StudentRepository");
        System.out.println("   - Parameter 2: StudentMapper");
        System.out.println("   - Looked for beans of these types");
        System.out.println("   - Found StudentRepository (from @Repository)");
        System.out.println("   - Found StudentMapper (from @Component)");
        System.out.println("   - Created both if needed");
        System.out.println("   - Called constructor with instances");

        System.out.println("\n2. CURRENT STATE:");
        System.out.println("   studentRepository = " +
            (studentRepository != null ? "INJECTED ✓" : "NULL ✗"));
        System.out.println("   studentMapper = " +
            (studentMapper != null ? "INJECTED ✓" : "NULL ✗"));

        System.out.println("\n3. USAGE IN METHODS:");
        System.out.println("   - getStudents() uses studentRepository");
        System.out.println("   - searchAndMapStudents() uses both");
        System.out.println("   - All methods can safely use injected beans");

        System.out.println("\n4. WHY CONSTRUCTOR INJECTION?");
        System.out.println("   ✓ Fields are final (immutable)");
        System.out.println("   ✓ Explicit in method signature");
        System.out.println("   ✓ Easy to test (pass mocks to constructor)");
        System.out.println("   ✓ Fails at startup if dependency missing");
        System.out.println("   ✓ No NullPointerException risk");

        System.out.println("\n5. INJECTION TIMELINE:");
        System.out.println("   constructor()           → this.repo = null, this.mapper = null");
        System.out.println("   ↓ (Spring resolves)");
        System.out.println("   constructor(repo, m)    → this.repo = repo, this.mapper = m");
        System.out.println("   ↓");
        System.out.println("   @PostConstruct init()   → Dependencies verified");
        System.out.println("   ↓");
        System.out.println("   Bean ready for requests");

        System.out.println("\n" + "═".repeat(70) + "\n");
    }
}

