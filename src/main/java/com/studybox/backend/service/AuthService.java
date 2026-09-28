package com.studybox.backend.service;

import com.studybox.backend.domain.Student;
import com.studybox.backend.dto.AuthResponse;
import com.studybox.backend.dto.LoginRequest;
import com.studybox.backend.dto.RegisterRequest;
import com.studybox.backend.dto.StudentProfileResponse;
import com.studybox.backend.exception.BadRequestException;
import com.studybox.backend.repository.StudentRepository;
import com.studybox.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StudentRepository studentRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Student student = studentRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), student.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (student.getFirstLoginDate() == null) {
            student.setFirstLoginDate(LocalDate.now());
        }
        student.setLastLoginAt(LocalDateTime.now());
        student.setTotalLogins(student.getTotalLogins() + 1);
        studentRepository.save(student);

        String token = jwtService.generateToken(student.getId(), student.getEmail());
        return new AuthResponse(token, toProfile(student));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Student student = Student.builder()
                .studentNumber(generateStudentNumber())
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .programme(request.programme() != null ? request.programme()
                        : "Bachelor of Science (BSc) (Honours) in Computer Science")
                .firstLoginDate(LocalDate.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
        studentRepository.save(student);

        String token = jwtService.generateToken(student.getId(), student.getEmail());
        return new AuthResponse(token, toProfile(student));
    }

    private String generateStudentNumber() {
        return "10" + (100000 + new java.util.Random().nextInt(899999));
    }

    public static StudentProfileResponse toProfile(Student student) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        long daysEnrolled = student.getFirstLoginDate() != null
                ? Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(student.getFirstLoginDate(), LocalDate.now()) + 1)
                : 1;
        int expectedLogins = (int) Math.max(1, daysEnrolled / 2); // a login every ~2 days is "full" frequency
        int frequencyPercent = (int) Math.min(100, Math.round(100.0 * student.getTotalLogins() / expectedLogins));

        return new StudentProfileResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getFullName(),
                student.getEmail(),
                student.getProgramme(),
                student.getPhotoUrl(),
                student.getFirstLoginDate() != null ? student.getFirstLoginDate().format(dateFmt) : null,
                student.getLastLoginAt() != null ? student.getLastLoginAt().format(dateTimeFmt) : null,
                student.getTotalLogins(),
                frequencyPercent
        );
    }
}
