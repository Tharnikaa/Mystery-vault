package com.mysteryvault;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone JUnit 5 test runner for Member 1 test suites.
 * Executes all tests with reflection, reporting exact pass/fail status,
 * ensuring zero external runner setup is required.
 */
public class Member1TestSuite {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   MEMBER 1 AUTOMATED TEST SUITE EXECUTION");
        System.out.println("================================================================================\n");

        Class<?>[] testClasses = new Class<?>[] {
                CaseStatusTest.class,
                CaseServiceTest.class,
                SuspectTest.class,
                EvidenceTest.class,
                ClueUnlockEngineTest.class
        };

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        List<String> failures = new ArrayList<>();

        for (Class<?> testClass : testClasses) {
            System.out.println("--> Running " + testClass.getSimpleName() + "...");
            Method[] methods = testClass.getDeclaredMethods();

            // Find @BeforeEach method if any
            Method beforeEachMethod = null;
            for (Method m : methods) {
                if (m.isAnnotationPresent(BeforeEach.class)) {
                    beforeEachMethod = m;
                    beforeEachMethod.setAccessible(true);
                    break;
                }
            }

            for (Method m : methods) {
                if (m.isAnnotationPresent(Test.class)) {
                    totalTests++;
                    String displayName = m.isAnnotationPresent(DisplayName.class)
                            ? m.getAnnotation(DisplayName.class).value()
                            : m.getName();

                    try {
                        Object instance = testClass.getDeclaredConstructor().newInstance();
                        if (beforeEachMethod != null) {
                            beforeEachMethod.invoke(instance);
                        }
                        m.setAccessible(true);
                        m.invoke(instance);
                        passedTests++;
                        System.out.printf("   [PASS] %s (%s)\n", displayName, m.getName());
                    } catch (Throwable t) {
                        failedTests++;
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        String failureMsg = testClass.getSimpleName() + "." + m.getName() + " -> " + cause.getMessage();
                        failures.add(failureMsg);
                        System.out.printf("   [FAIL] %s (%s): %s\n", displayName, m.getName(), cause.getMessage());
                    }
                }
            }
            System.out.println();
        }

        System.out.println("================================================================================");
        System.out.printf("   TEST RESULTS: %d Total | %d Passed | %d Failed\n", totalTests, passedTests, failedTests);
        if (failedTests > 0) {
            System.out.println("   FAILURES:");
            for (String f : failures) {
                System.out.println("     - " + f);
            }
        } else {
            System.out.println("   ALL TESTS PASSED SUCCESSFULLY! (100% SUCCESS RATE)");
        }
        System.out.println("================================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }
}
