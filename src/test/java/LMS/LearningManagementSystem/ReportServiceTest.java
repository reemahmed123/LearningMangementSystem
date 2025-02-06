package LMS.LearningManagementSystem;


import LMS.LearningManagementSystem.model.*;
import LMS.LearningManagementSystem.repository.*;
import LMS.LearningManagementSystem.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AssignmentLogRepository assignmentLogRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizLogRepository quizLogRepository;


    @InjectMocks
    private ReportService reportService;

    private Student student;
    private Course course;
    private Assignment assignment;
    private AssignmentLog assignmentLog;
    private Attendance attendance;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        student = new Student();
        student.setId(1);
        student.setName("John Doe");

        course = new Course();
        course.setId(1);

        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCourse(course); // Associate Lesson with Course

        attendance = new Attendance();
        attendance.setAttended(true);
        attendance.setStudent(student);
        attendance.setLesson(lesson); // Associate Attendance with Lesson

        assignment = new Assignment();
        assignment.setId(1); // Set an ID for the assignment
        assignment.setCourseId(1);

        assignmentLog = new AssignmentLog();
        assignmentLog.setGrade(90);
        assignmentLog.setStudentId(student.getId());
        assignmentLog.setAssignment(assignment);

        quiz = new Quiz();
        quiz.setId(1);
        quiz.setCourse(course);
// Add more initialization as needed

    }

    @Test
    void testGenerateExcelReport() throws IOException {
        // Arrange
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);
        when(assignmentRepository.findAllByCourseId(1)).thenReturn(Arrays.asList(assignment));
        when(assignmentLogRepository.findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId())))
                .thenReturn(Arrays.asList(assignmentLog));
        when(attendanceRepository.findAll()).thenReturn(Arrays.asList(attendance));

        byte[] report = reportService.generateExcelReport(1);

        // Assert
        assertNotNull(report);
        assertTrue(report.length > 0);

        // Verify that all necessary methods were called
        verify(studentRepository, times(1)).findAll();
        verify(assignmentRepository, times(1)).findAllByCourseId(1);
        verify(assignmentLogRepository, times(1)).findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId()));
        verify(attendanceRepository, times(1)).findAll();
    }

    @Test
    void testGeneratePerformanceChart() throws IOException {
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);
        when(assignmentRepository.findAllByCourseId(1)).thenReturn(Arrays.asList(assignment));
        when(assignmentLogRepository.findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId())))
                .thenReturn(Arrays.asList(assignmentLog));

        byte[] chartData = reportService.generatePerformanceChart(1);

        // Assert
        assertNotNull(chartData);
        assertTrue(chartData.length > 0);

        // Verify method calls
        verify(studentRepository, times(1)).findAll();
        verify(assignmentRepository, times(1)).findAllByCourseId(1);
        verify(assignmentLogRepository, times(1)).findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId()));
    }

    @Test
    void testCalculateTotalGrades() {
        // Mock assignmentRepository
        when(assignmentRepository.findAllByCourseId(1)).thenReturn(Arrays.asList(assignment));
        when(assignmentLogRepository.findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId())))
                .thenReturn(Arrays.asList(assignmentLog));

        // Mock quizRepository and quizLogRepository
        when(quizRepository.findAllByCourseId(1)).thenReturn(Arrays.asList(quiz));
        when(quizLogRepository.findAllByStudentIdAndQuizIdIn(student.getId(), Arrays.asList(quiz.getId())))
                .thenReturn(Collections.emptyList()); // Assuming no quizzes for this test

        int totalGrades = reportService.calculateTotalGrades(student.getId(), 1);

        // Assert
        assertEquals(90, totalGrades); // The total grades should come from assignment logs only
        verify(assignmentRepository, times(1)).findAllByCourseId(1);
        verify(assignmentLogRepository, times(1)).findAllByStudentIdAndAssignmentIdIn(student.getId(), Arrays.asList(assignment.getId()));
        verify(quizRepository, times(1)).findAllByCourseId(1);
        verify(quizLogRepository, times(1)).findAllByStudentIdAndQuizIdIn(student.getId(), Arrays.asList(quiz.getId()));
    }


    @Test
    void testCalculateAttendance() {
        // Arrange: Mock the repository to return attendance data
        when(attendanceRepository.findAll()).thenReturn(Arrays.asList(attendance));

        int attendanceCount = reportService.calculateAttendance(student.getId(), course.getId());

        // Assert
        assertEquals(1, attendanceCount); // The attendance count should be 1
        verify(attendanceRepository, times(1)).findAll();
    }
}