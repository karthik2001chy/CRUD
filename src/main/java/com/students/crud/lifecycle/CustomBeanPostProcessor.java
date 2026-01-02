package com.students.crud.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * CUSTOM BeanPostProcessor - Demonstrates what happens BEFORE and AFTER initialization
 *
 * Spring calls these methods for EVERY bean in the container:
 *
 * 1. postProcessBeforeInitialization() - Called BEFORE @PostConstruct, @Autowired
 * 2. postProcessAfterInitialization()  - Called AFTER @PostConstruct, @Autowired
 *
 * This is how Spring implements:
 * - @Transactional (creates proxy)
 * - @Cacheable (wraps method calls)
 * - @Async (creates async proxy)
 *
 * EXECUTION ORDER for each bean:
 * ┌────────────────────────────────────────┐
 * │ 1. Constructor                         │
 * │ 2. Dependency Injection                │
 * │ 3. Aware interfaces (setBeanName, etc) │
 * │ 4. postProcessBeforeInitialization()   │ ← HERE
 * │ 5. @PostConstruct                      │
 * │ 6. InitializingBean.afterPropertiesSet │
 * │ 7. postProcessAfterInitialization()    │ ← HERE
 * │ 8. Bean ready to use                   │
 * └────────────────────────────────────────┘
 *
 * Example: How @Cacheable works:
 *
 * Original code:
 * @Cacheable(value = "studentSimple")
 * public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
 *     return studentRepository.findAll(pageable).map(studentMapper::toSimpleDto);
 * }
 *
 * What Spring does in postProcessAfterInitialization():
 *
 * StudentService original = (StudentService) bean;
 *
 * StudentService proxy = new CachingProxy(original) {
 *     @Override
 *     public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
 *         String key = "studentSimple:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
 *
 *         // Check cache first
 *         if (cache.contains(key)) {
 *             System.out.println("Cache HIT!");
 *             return cache.get(key);
 *         }
 *
 *         // Cache miss - call original method
 *         System.out.println("Cache MISS - calling original");
 *         Page<StudentSimpleDto> result = original.getStudentsSimple(pageable);
 *
 *         // Store in cache
 *         cache.put(key, result);
 *
 *         return result;
 *     }
 * };
 *
 * return proxy;  // Return proxy instead of original
 */
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    private static int beanCount = 0;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        System.out.println("\n  [BeanPostProcessor.BEFORE] Bean: " + beanName);
        System.out.println("    Class: " + bean.getClass().getSimpleName());
        System.out.println("    ├─ Dependency injection completed");
        System.out.println("    ├─ About to call @PostConstruct");
        System.out.println("    └─ Can modify bean before initialization");

        // Interesting beans to highlight
        if (beanName.contains("StudentService")) {
            System.out.println("    ⚠️  This is StudentService - will be proxied for @Cacheable!");
        }
        if (beanName.contains("StudentController")) {
            System.out.println("    ⚠️  This is StudentController - depends on StudentService!");
        }
        if (beanName.contains("RedisCacheManager")) {
            System.out.println("    ⚠️  This is Redis cache configuration!");
        }

        beanCount++;
        return bean;  // Return original bean (can return modified or proxy)
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {

        System.out.println("  [BeanPostProcessor.AFTER] Bean: " + beanName);
        System.out.println("    Class: " + bean.getClass().getSimpleName());
        System.out.println("    ├─ @PostConstruct has completed");
        System.out.println("    ├─ Creating proxies for @Transactional, @Cacheable, @Async");
        System.out.println("    └─ Bean is now ready to use");

        // In real Spring, this is where proxies are created
        // For StudentService, Spring would create a caching proxy here

        if (beanName.contains("StudentService")) {
            System.out.println("    ✓ Created CachingProxy for StudentService");
            System.out.println("      - Wraps getStudentsSimple() method");
            System.out.println("      - Checks Redis cache before calling method");
        }

        return bean;  // Return original bean (or return proxy in real scenarios)
    }

    // Called by Spring after all beans are initialized
    public static int getProcessedBeanCount() {
        return beanCount;
    }
}

