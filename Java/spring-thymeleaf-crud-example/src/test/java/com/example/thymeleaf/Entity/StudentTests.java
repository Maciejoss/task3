package com.example.thymeleaf;

import com.example.thymeleaf.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudentTests {

    private Student student;
    private Validator validator;

    @BeforeEach
    void setUp() {
        student = new Student();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateStudentWithValidData() {
        // Arrange
        student.setName("Jan Kowalski");
        student.setEmail("jan.kowalski@example.com");
        student.setBirthday(LocalDate.of(2000, 1, 1));

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldGenerateIdOnPrePersist() throws Exception {
        // Use reflection to invoke the private method
        Method prePersistMethod = Student.class.getDeclaredMethod("prePersist");
        prePersistMethod.setAccessible(true);

        // Act
        prePersistMethod.invoke(student);

        // Assert
        assertNotNull(student.getId());
        assertNotNull(student.getCreatedAt());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        // Arrange
        student.setName(null);

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        // Arrange
        student.setName("Jan Kowalski");
        student.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        // Arrange
        student.setName("Jan Kowalski");
        student.setEmail("jan.kowalski@example.com");
        student.setBirthday(LocalDate.now().plusDays(1));

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameContainsSQLInjection() {
        // Arrange
        student.setName("Robert'); DROP TABLE Students;--");
        student.setEmail("jan.kowalski@example.com");

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailContainsJavaScriptInjection() {
        // Arrange
        student.setName("Jan Kowalski");
        student.setEmail("<script>alert('xss')</script>");

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsTooLong() {
        // Arrange
        student.setName("a".repeat(256)); // Assuming max length is 255
        student.setEmail("jan.kowalski@example.com");

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenBirthdayIsTooOld() {
        // Arrange
        student.setName("Jan Kowalski");
        student.setEmail("jan.kowalski@example.com");
        student.setBirthday(LocalDate.of(1800, 1, 1));

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameContainsSpecialCharacters() {
        // Arrange
        student.setName("@@@@@@@@");
        student.setEmail("jan.kowalski@example.com");

        // Act
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        // Assert
        assertFalse(violations.isEmpty());
    }
}
