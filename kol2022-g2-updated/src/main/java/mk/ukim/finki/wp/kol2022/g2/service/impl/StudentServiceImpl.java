package mk.ukim.finki.wp.kol2022.g2.service.impl;

import mk.ukim.finki.wp.kol2022.g2.model.Course;
import mk.ukim.finki.wp.kol2022.g2.model.Student;
import mk.ukim.finki.wp.kol2022.g2.model.StudentType;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidStudentIdException;
import mk.ukim.finki.wp.kol2022.g2.repository.CourseRepository;
import mk.ukim.finki.wp.kol2022.g2.repository.StudentRepository;
import mk.ukim.finki.wp.kol2022.g2.service.CourseService;
import mk.ukim.finki.wp.kol2022.g2.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;

    private final CourseService courseService;

    public StudentServiceImpl(StudentRepository studentRepository, PasswordEncoder passwordEncoder, CourseRepository courseRepository, CourseService courseService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
    }


    @Override
    public List<Student> listAll() {
        return this.studentRepository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return this.studentRepository.findById(id).orElseThrow(InvalidStudentIdException::new);
    }

    @Override
    public Student create(String name, String email, String password, StudentType type, List<Long> courseId, LocalDate enrollmentDate) {
        List<Course> courses = this.courseRepository.findAllById(courseId);
        Student s = new Student(name, email, passwordEncoder.encode(password), type, courses, enrollmentDate);
        return this.studentRepository.save(s);
    }

    @Override
    public Student update(Long id, String name, String email, String password, StudentType type, List<Long> coursesId, LocalDate enrollmentDate) {
        Student s = this.findById(id);
        List<Course> courses = this.courseRepository.findAllById(coursesId);
        s.setName(name);
        s.setEmail(email);
        s.setCourses(courses);
        s.setPassword(passwordEncoder.encode(password));
        s.setEnrollmentDate(enrollmentDate);
        s.setType(type);
        return this.studentRepository.save(s);
    }

    @Override
    public Student delete(Long id) {
        Student s = this.findById(id);
        this.studentRepository.delete(s);
        return s;
    }

    @Override
    public List<Student> filter(Long courseId, Integer yearsOfStudying) {
        if(courseId==null && yearsOfStudying==null)
        {
            return listAll();
        }
        else if(courseId==null)
        {
            return studentRepository.findByEnrollmentDateBefore(LocalDate.now().minusYears(yearsOfStudying));
        }
        else if(yearsOfStudying==null)
        {
            return studentRepository.findAllByCoursesContaining(courseService.findById(courseId));
        }
        else
        {
            return studentRepository.findAllByCoursesContainingAndAndEnrollmentDateBefore(courseService.findById(courseId),LocalDate.now().minusYears(yearsOfStudying));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Student student = studentRepository.findByEmail(username);

        return User.builder()
                .username(student.getEmail())
                .password(student.getPassword())
                .authorities("ROLE_"+student.getType())
                .build();
    }
}
