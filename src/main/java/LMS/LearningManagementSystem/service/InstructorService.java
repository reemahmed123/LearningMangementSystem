package LMS.LearningManagementSystem.service;

import LMS.LearningManagementSystem.model.*;
import LMS.LearningManagementSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InstructorService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentLogRepository assignmentLogRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final NotificationService notificationService;
    private final LessonService lessonService;
    private final LessonRepository lessonRepository;


    @Autowired
    public InstructorService(InstructorRepository instructorRepository, CourseRepository courseRepository, AssignmentRepository assignmentRepository, AssignmentLogRepository assignmentLogRepository, QuestionRepository questionRepository, QuizRepository quizRepository, NotificationService notificationService, LessonService lessonService, LessonRepository lessonRepository){
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentLogRepository = assignmentLogRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.notificationService = notificationService;
        this.lessonService = lessonService;
        this.lessonRepository = lessonRepository;
    }

    public void addLesson(int instructorId, Lesson lesson, int courseId) throws Exception {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructor().getId() != instructorId) {
            throw new Exception("Instructor does not have permission to add lessons to this course.");
        }
        lesson.setOtp(lessonService.generateOtp());
        lesson.setCourse(course);
        String title = lesson.getTitle();
        lesson.setTitle(title);
        lessonRepository.save(lesson);
    }


    public void createQuiz(Integer instructorId, Integer courseId, String title, Integer numberOfQuestions){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructor().getId() != instructorId) {
            throw new IllegalArgumentException("Instructor does not have permission to modify this course.");
        }

        List<Question> questions = questionRepository.findByCourseId(courseId);

        Set<Question> questionSet = new HashSet<>();

        while (questionSet.size() < numberOfQuestions) {
            int questionNumber = (int) (Math.random() * questions.size());
            questionSet.add(questions.get(questionNumber));
        }
        questions =new ArrayList<>(questionSet);

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(title);
        quiz.setNumberOfQuestions(numberOfQuestions);
        quiz.setTotalGrade(numberOfQuestions);
        quizRepository.save(quiz);
        String notification = quiz.getTitle() + "has been added to " + quiz.getCourse().getCourseTitle();
        List<Student> students = course.getEnrolledStudents();
        for(Student student : students){
            notificationService.createNotification(student.getId(),Role.Student, notification);
        }
    }

    public void createQuestion(Integer instructorId, Question question){
        Course course = courseRepository.findById(question.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Ensure the instructor owns the course
        if (course.getInstructor().getId() != instructorId) {
            throw new IllegalArgumentException("Instructor does not have permission to modify this course.");
        }
        questionRepository.save(question);
    }

}