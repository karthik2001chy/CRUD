package com.students.crud.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * DEMONSTRATION CLASS - Shows all lifecycle hooks in action
 *
 * This class implements all possible interfaces and annotations
 * to show the EXACT ORDER in which Spring calls lifecycle methods.
 *
 * EXECUTION ORDER:
 * 1. Constructor
 * 2. Aware interfaces (setBeanName, setApplicationContext)
 * 3. BeanPostProcessor.postProcessBeforeInitialization()
 * 4. @PostConstruct
 * 5. InitializingBean.afterPropertiesSet()
 * 6. init-method (not used here - XML only)
 * 7. BeanPostProcessor.postProcessAfterInitialization()
 *
 * === STARTUP ===
 * [Constructor] BeanLifecycleDemo constructor called
 * [Aware] setBeanName: beanLifecycleDemo
 * [Aware] setApplicationContext: application context injected
 * [Before Init] BeanPostProcessor.postProcessBeforeInitialization()
 * [PostConstruct] @PostConstruct method called
 * [Initializing] InitializingBean.afterPropertiesSet()
 * [After Init] BeanPostProcessor.postProcessAfterInitialization()
 * [Ready] Bean is now ready to use!
 *
 * === SHUTDOWN ===
 * [PreDestroy] @PreDestroy method called - cleanup
 * [Dispose] DisposableBean.destroy()
 */
@Component
public class BeanLifecycleDemo
        implements BeanNameAware,
                   ApplicationContextAware,
                   InitializingBean,
                   DisposableBean {

    // ==================== PHASE 1: CONSTRUCTION ====================

    public BeanLifecycleDemo() {
        System.out.println("\n" + color("CYAN", "█ [PHASE 1 - CONSTRUCTION]"));
        System.out.println(color("GREEN", "✓ 1. Constructor called"));
        System.out.println("  - Object is created with new keyword");
        System.out.println("  - Bean is NOT yet fully initialized");
        System.out.println("  - Dependencies are NOT YET injected");
    }

    // ==================== PHASE 2: AWARE INTERFACES ====================

    private String beanName;
    private ApplicationContext applicationContext;

    @Override
    public void setBeanName(String name) {
        System.out.println(color("GREEN", "✓ 2. setBeanName() called"));
        System.out.println("  - Bean name from container: " + color("YELLOW", name));
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        System.out.println(color("GREEN", "✓ 3. setApplicationContext() called"));
        System.out.println("  - ApplicationContext is injected");
        System.out.println("  - Can now access any bean from container at runtime");
        this.applicationContext = context;
    }

    // Note: BeanPostProcessor methods are called here but they're in a separate class

    // ==================== PHASE 3: INITIALIZATION ====================

    @PostConstruct
    public void postConstructMethod() {
        System.out.println("\n" + color("CYAN", "█ [PHASE 3 - INITIALIZATION]"));
        System.out.println(color("GREEN", "✓ 4. @PostConstruct method called"));
        System.out.println("  - All dependencies are now available");
        System.out.println("  - All Aware interfaces have been called");
        System.out.println("  - Good place to initialize resources");
        System.out.println("  - Example: Load cache, establish connections");

        // Example initialization
        initializeResources();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(color("GREEN", "✓ 5. afterPropertiesSet() called (InitializingBean)"));
        System.out.println("  - Alternative to @PostConstruct");
        System.out.println("  - Deprecated in modern Spring (use @PostConstruct)");
    }

    private void initializeResources() {
        System.out.println("  Initializing resources...");
        System.out.println("  - Loading student cache from database");
        System.out.println("  - Connecting to Redis for caching");
        System.out.println("  - Setting up monitoring/metrics");
    }

    // ==================== PHASE 4: READY TO USE ====================

    public void demonstrateUsage() {
        System.out.println("\n" + color("CYAN", "█ [PHASE 4 - USAGE]"));
        System.out.println(color("GREEN", "✓ 6. Bean is ready for requests"));
        System.out.println("  - HTTP requests can now be processed");
        System.out.println("  - Controller calls Service");
        System.out.println("  - Service calls Repository");
        System.out.println("  - Repository queries database");
    }

    // ==================== PHASE 5: DESTRUCTION ====================

    @PreDestroy
    public void preDestroyMethod() {
        System.out.println("\n" + color("CYAN", "█ [PHASE 5 - DESTRUCTION]"));
        System.out.println(color("RED", "✗ 7. @PreDestroy method called"));
        System.out.println("  - Application is shutting down");
        System.out.println("  - Clean up any resources");
        System.out.println("  - Flush caches, close connections");

        cleanupResources();
    }

    @Override
    public void destroy() throws Exception {
        System.out.println(color("RED", "✗ 8. destroy() called (DisposableBean)"));
        System.out.println("  - Final cleanup before removal from memory");
        System.out.println("  - Alternative to @PreDestroy (deprecated)");
    }

    private void cleanupResources() {
        System.out.println("  Cleaning up resources...");
        System.out.println("  - Closing database connections");
        System.out.println("  - Closing Redis connections");
        System.out.println("  - Saving state to file");
    }

    // ==================== HELPER METHOD ====================

    private static String color(String color, String text) {
        return switch(color) {
            case "RED" -> "\033[91m" + text + "\033[0m";
            case "GREEN" -> "\033[92m" + text + "\033[0m";
            case "YELLOW" -> "\033[93m" + text + "\033[0m";
            case "CYAN" -> "\033[96m" + text + "\033[0m";
            default -> text;
        };
    }
}

