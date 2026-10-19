package com.mysteryvault;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone JUnit 5 test runner executing both Member 1 & Member 3 test suites.
 */
public class Member3TestSuite {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   MEMBER 3 AUTOMATED TEST SUITE EXECUTION");
        System.out.println("================================================================================\n");

        Class<?>[] testClasses = new Class<?>[] {
                Member3Test.class,
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
                        System.out.println("   [PASS] " + displayName + " (" + m.getName() + ")");
                    } catch (Throwable t) {
                        failedTests++;
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        String failMsg = testClass.getSimpleName() + "." + m.getName() + " -> " + cause.getMessage();
                        failures.add(failMsg);
                        System.err.println("   [FAIL] " + displayName + " (" + m.getName() + ")");
                        System.err.println("          Error: " + cause.getMessage());
                    }
                }
            }
            System.out.println();
        }

        System.out.println("================================================================================");
        System.out.printf("   TEST RESULTS: %d Total | %d Passed | %d Failed%n", totalTests, passedTests, failedTests);
        if (failedTests == 0) {
            System.out.println("   ALL TESTS PASSED SUCCESSFULLY! (100% SUCCESS RATE)");
        } else {
            System.out.println("   FAILURES ENCOUNTERED:");
            for (String f : failures) {
                System.out.println("     - " + f);
            }
        }
        System.out.println("================================================================================\n");

        if (failedTests > 0) {
            System.exit(1);
        }
    }
}
