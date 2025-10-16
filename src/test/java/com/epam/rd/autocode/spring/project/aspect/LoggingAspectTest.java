package com.epam.rd.autocode.spring.project.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for SimpleLoggingAspect
 */
@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        lenient().when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        lenient().when(joinPoint.getSignature().getName()).thenReturn("testMethod");
        lenient().when(joinPoint.getSignature().toShortString()).thenReturn("TestClass.testMethod(..)");
        lenient().when(joinPoint.getTarget()).thenReturn(new Object());
        lenient().when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
    }

    @Test
    void testLogAroundController_Success() throws Throwable {
        // Given
        String expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = loggingAspect.logController(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testLogAroundController_Exception() throws Throwable {
        // Given
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(joinPoint.proceed()).thenThrow(expectedException);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            loggingAspect.logController(joinPoint);
        });

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testLogAroundService_Success() throws Throwable {
        // Given
        String expectedResult = "service result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = loggingAspect.logService(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testLogAroundRepository_Success() throws Throwable {
        // Given
        String expectedResult = "repository result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = loggingAspect.logRepository(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(joinPoint, times(1)).proceed();
    }
}
