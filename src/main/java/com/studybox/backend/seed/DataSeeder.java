package com.studybox.backend.seed;

import com.studybox.backend.domain.*;
import com.studybox.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Populates the database with a realistic, self-contained demo dataset on first run.
 * This is sample data only — it is not synced with any real institution's system.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final EducatorRepository educatorRepository;
    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeCategoryRepository gradeCategoryRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AnnouncementRepository announcementRepository;
    private final InvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;
    private final CatalogCourseRepository catalogCourseRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ConversationRepository conversationRepository;
    private final DiscussionThreadRepository discussionThreadRepository;
    private final FileResourceRepository fileResourceRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean forceSeed;
    private final boolean demoDataEnabled;

    private final Map<String, Educator> educatorCache = new HashMap<>();

    public DataSeeder(
            StudentRepository studentRepository,
            EducatorRepository educatorRepository,
            CourseRepository courseRepository,
            CourseInstructorRepository courseInstructorRepository,
            EnrollmentRepository enrollmentRepository,
            CourseModuleRepository courseModuleRepository,
            AssignmentRepository assignmentRepository,
            GradeCategoryRepository gradeCategoryRepository,
            TimetableEntryRepository timetableEntryRepository,
            AnnouncementRepository announcementRepository,
            InvoiceRepository invoiceRepository,
            InstallmentRepository installmentRepository,
            CatalogCourseRepository catalogCourseRepository,
            ClassSessionRepository classSessionRepository,
            ConversationRepository conversationRepository,
            DiscussionThreadRepository discussionThreadRepository,
            FileResourceRepository fileResourceRepository,
            AdminAccountRepository adminAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${studybox.seed.force:false}") boolean forceSeed,
            @Value("${studybox.seed.demo-data:false}") boolean demoDataEnabled) {
        this.studentRepository = studentRepository;
        this.educatorRepository = educatorRepository;
        this.courseRepository = courseRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.assignmentRepository = assignmentRepository;
        this.gradeCategoryRepository = gradeCategoryRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.announcementRepository = announcementRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.catalogCourseRepository = catalogCourseRepository;
        this.classSessionRepository = classSessionRepository;
        this.conversationRepository = conversationRepository;
        this.discussionThreadRepository = discussionThreadRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.forceSeed = forceSeed;
        this.demoDataEnabled = demoDataEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // The one bit of bootstrap data that always exists, demo data or not — otherwise
        // there'd be no way to log into the Super Admin dashboard on a brand-new database.
        seedAdminAccountIfMissing();

        // Clean slate by default: no courses, instructors, timetable, announcements or
        // students are created automatically — everything is added from the Super Admin
        // dashboard. The full sample dataset only loads when studybox.seed.demo-data=true.
        if (!demoDataEnabled) {
            return;
        }

        if (!forceSeed && courseRepository.count() > 0) {
            return;
        }

        // Always seed the programme's structure — courses, instructors, weekly content,
        // the timetable, and the enrollment catalogue. This is configuration, not "dummy
        // student data", and the Super Admin needs courses to exist before it can enroll
        // anyone in them (it can also create brand-new ones of its own).
        List<CourseBlueprint> blueprints = courseBlueprints();
        List<Course> courses = new ArrayList<>();

        int index = 0;
        for (CourseBlueprint bp : blueprints) {
            Course course = seedCourse(bp);
            courses.add(course);
            seedInstructors(course, bp);
            seedModules(course);
            seedTimetable(course, bp);
            if (demoDataEnabled) {
                List<Assignment> assignments = seedAssignments(course, index);
                seedGrades(course, assignments, index);
            }
            index++;
        }

        seedAnnouncements(blueprints);
        seedCatalog();

        // Everything below is the fictional demo narrative (a sample student, their
        // classmates, activity, and history) — off by default. Turn it on with
        // studybox.seed.demo-data=true for a filled-in sandbox to explore; leave it off
        // (the default) to start with a clean slate that only the Super Admin populates.
        if (demoDataEnabled) {
            Student student = seedStudent();
            for (int i = 0; i < courses.size(); i++) {
                seedEnrollment(student, courses.get(i), i);
                seedClassSessions(courses.get(i), student, i);
                seedDiscussionThread(courses.get(i), blueprints.get(i), i);
            }

            List<Student> classmates = seedClassmates();
            seedClassmateEnrollments(classmates, courses);

            seedFinancial(student);
            seedMessaging(student, blueprints);
            seedFiles(student, courses);
        }
    }

    // ------------------------------------------------------------ admin account

    private void seedAdminAccountIfMissing() {
        if (adminAccountRepository.count() > 0) {
            return;
        }
        adminAccountRepository.save(AdminAccount.builder()
                .fullName("Programme Administrator")
                .email("admin@studybox.app")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role("ADMIN")
                .build());
    }

    // ---------------------------------------------------------------- student

    private Student seedStudent() {
        Student student = Student.builder()
                .studentNumber("ST-100213")
                .fullName("Jordan Blake")
                .email("demo.student@studybox.app")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .programme("Bachelor of Science (BSc) (Honours) in Computer Science")
                .firstLoginDate(LocalDate.now().minusMonths(6))
                .build();
        return studentRepository.save(student);
    }

    // ---------------------------------------------------------------- courses

    private record InstructorBlueprint(String name, String title, String email) {
    }

    private record CourseBlueprint(
            String code, String title, int credits, String colorHex, String icon,
            List<InstructorBlueprint> instructors, DayOfWeek day, LocalTime start, LocalTime end, String room) {
    }

    private List<CourseBlueprint> courseBlueprints() {
        InstructorBlueprint lenaOrtiz = new InstructorBlueprint("Lena Ortiz", "Adjunct Lecturer", "lena.ortiz@faculty.studybox.app");
        InstructorBlueprint jordanWells = new InstructorBlueprint("Jordan Wells", "Tutor", "jordan.wells@faculty.studybox.app");
        InstructorBlueprint marcusReid = new InstructorBlueprint("Marcus Reid", "Adjunct Professor", "marcus.reid@faculty.studybox.app");
        InstructorBlueprint sofiaBianchi = new InstructorBlueprint("Sofia Bianchi", "Tutor", "sofia.bianchi@faculty.studybox.app");
        InstructorBlueprint priyaNandan = new InstructorBlueprint("Priya Nandan", "Tutor", "priya.nandan@faculty.studybox.app");
        InstructorBlueprint omarHaddad = new InstructorBlueprint("Omar Haddad", "Adjunct Lecturer", "omar.haddad@faculty.studybox.app");
        InstructorBlueprint elenaPetrova = new InstructorBlueprint("Elena Petrova", "Adjunct Lecturer", "elena.petrova@faculty.studybox.app");
        InstructorBlueprint davidKim = new InstructorBlueprint("David Kim", "Tutor", "david.kim@faculty.studybox.app");

        return List.of(
                new CourseBlueprint("COMP-5007", "Cloud Computing Automation and Ops", 6, "#6366F1", "cloud",
                        List.of(lenaOrtiz, jordanWells), DayOfWeek.MONDAY, LocalTime.of(16, 30), LocalTime.of(18, 0), "Online - Zoom"),
                new CourseBlueprint("COMP-5008", "Front-end Programming", 6, "#F97316", "code",
                        List.of(marcusReid, sofiaBianchi), DayOfWeek.TUESDAY, LocalTime.of(16, 30), LocalTime.of(18, 0), "Online - Zoom"),
                new CourseBlueprint("COMP-5009", "Mobile Programming", 6, "#10B981", "smartphone",
                        List.of(marcusReid, sofiaBianchi), DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(18, 30), "Online - Zoom"),
                new CourseBlueprint("COMP-5010", "Software Engineering", 6, "#3B82F6", "build",
                        List.of(priyaNandan, omarHaddad), DayOfWeek.THURSDAY, LocalTime.of(16, 30), LocalTime.of(18, 0), "Online - Zoom"),
                new CourseBlueprint("CYS-8005", "Secure Software Development", 6, "#EF4444", "lock",
                        List.of(elenaPetrova, davidKim), DayOfWeek.FRIDAY, LocalTime.of(15, 0), LocalTime.of(16, 30), "Online - Zoom")
        );
    }

    private Course seedCourse(CourseBlueprint bp) {
        Course course = Course.builder()
                .code(bp.code())
                .title(bp.title())
                .credits(bp.credits())
                .term("Current Semester")
                .colorHex(bp.colorHex())
                .icon(bp.icon())
                .description("Sample course content for " + bp.title() + ".")
                .build();
        return courseRepository.save(course);
    }

    private void seedInstructors(Course course, CourseBlueprint bp) {
        for (InstructorBlueprint ib : bp.instructors()) {
            Educator educator = educatorCache.computeIfAbsent(ib.name(), name -> {
                Educator e = Educator.builder()
                        .fullName(ib.name())
                        .title(ib.title())
                        .email(ib.email())
                        .avatarInitials(initialsOf(ib.name()))
                        .build();
                return educatorRepository.save(e);
            });

            CourseInstructor link = CourseInstructor.builder()
                    .course(course)
                    .educator(educator)
                    .roleInCourse(ib.title())
                    .build();
            courseInstructorRepository.save(link);
        }
    }

    private String initialsOf(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ enrollment

    private void seedEnrollment(Student student, Course course, int index) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .presences(10 + index)
                .authorizedAbsences(index == 1 ? 1 : 0)
                .unauthorizedAbsences(index == 3 ? 1 : 0)
                .lates(index == 0 ? 1 : 0)
                .build();
        enrollmentRepository.save(enrollment);
    }

    // ---------------------------------------------------------------- modules

    private void seedModules(Course course) {
        CourseModule week1 = CourseModule.builder()
                .course(course).weekLabel("Week 1").title("Course Introduction").orderIndex(0)
                .build();
        List<ModuleItem> week1Items = new ArrayList<>();
        week1Items.add(ModuleItem.builder().module(week1).type(ModuleItemType.EXTERNAL_LINK)
                .title("Live Session 1 Recording").orderIndex(0).build());
        week1Items.add(ModuleItem.builder().module(week1).type(ModuleItemType.ATTACHMENT)
                .title("Slides: Administrative Introduction").orderIndex(1).build());
        week1.setItems(week1Items);
        courseModuleRepository.save(week1);

        CourseModule week2 = CourseModule.builder()
                .course(course).weekLabel("Week 2").title("Core Concepts").orderIndex(1)
                .build();
        List<ModuleItem> week2Items = new ArrayList<>();
        week2Items.add(ModuleItem.builder().module(week2).type(ModuleItemType.VIDEO)
                .title("Lecture: Foundations & Overview").orderIndex(0).build());
        week2Items.add(ModuleItem.builder().module(week2).type(ModuleItemType.VIDEO)
                .title("Lecture: Key Tools & Environment Setup").orderIndex(1).build());
        week2Items.add(ModuleItem.builder().module(week2).type(ModuleItemType.ATTACHMENT)
                .title("Slides: Core Concepts").orderIndex(2).build());
        week2Items.add(ModuleItem.builder().module(week2).type(ModuleItemType.QUIZ)
                .title("Practice Lab 1").dueAt(LocalDateTime.now().minusDays(10)).points(5.0).orderIndex(3).build());
        week2.setItems(week2Items);
        courseModuleRepository.save(week2);

        CourseModule week3 = CourseModule.builder()
                .course(course).weekLabel("Week 3").title("Applied Practice").orderIndex(2)
                .build();
        List<ModuleItem> week3Items = new ArrayList<>();
        week3Items.add(ModuleItem.builder().module(week3).type(ModuleItemType.LAB)
                .title("Practice Lab 2").dueAt(LocalDateTime.now().minusDays(3)).points(5.0).orderIndex(0).build());
        week3Items.add(ModuleItem.builder().module(week3).type(ModuleItemType.QUIZ)
                .title("Practice Lab 3").dueAt(LocalDateTime.now().plusDays(4)).points(5.0).orderIndex(1).build());
        week3.setItems(week3Items);
        courseModuleRepository.save(week3);
    }

    // ----------------------------------------------------------- assignments

    private List<Assignment> seedAssignments(Course course, int index) {
        List<Assignment> assignments = new ArrayList<>();

        assignments.add(assignmentRepository.save(Assignment.builder()
                .course(course).title("Practice Lab 1").type(AssignmentType.QUIZ).categoryLabel("Practice")
                .availableFrom(LocalDateTime.now().minusDays(24)).dueAt(LocalDateTime.now().minusDays(10))
                .pointsPossible(5).scoreEarned(4.5).status(AssignmentStatus.GRADED)
                .submittedAt(LocalDateTime.now().minusDays(11))
                .build()));

        assignments.add(assignmentRepository.save(Assignment.builder()
                .course(course).title("Practice Lab 2").type(AssignmentType.LAB).categoryLabel("Practice")
                .availableFrom(LocalDateTime.now().minusDays(17)).dueAt(LocalDateTime.now().minusDays(3))
                .pointsPossible(5).status(AssignmentStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(4))
                .build()));

        assignments.add(assignmentRepository.save(Assignment.builder()
                .course(course).title("Practice Lab 3").type(AssignmentType.QUIZ).categoryLabel("Practice")
                .availableFrom(LocalDateTime.now().minusDays(3)).dueAt(LocalDateTime.now().plusDays(4))
                .pointsPossible(5).status(AssignmentStatus.NOT_STARTED)
                .build()));

        boolean firstAssessmentGraded = index == 0 || index == 2;
        assignments.add(assignmentRepository.save(Assignment.builder()
                .course(course).title("Assessment 1").type(AssignmentType.ASSESSMENT).categoryLabel("Assessment 1")
                .availableFrom(LocalDateTime.now().minusDays(20)).dueAt(LocalDateTime.now().plusDays(9 - index))
                .pointsPossible(100)
                .scoreEarned(firstAssessmentGraded ? 82.0 : null)
                .status(firstAssessmentGraded ? AssignmentStatus.GRADED : AssignmentStatus.NOT_STARTED)
                .submittedAt(firstAssessmentGraded ? LocalDateTime.now().minusDays(2) : null)
                .build()));

        assignments.add(assignmentRepository.save(Assignment.builder()
                .course(course).title("Assessment 2").type(AssignmentType.ASSESSMENT).categoryLabel("Assessment 2")
                .availableFrom(LocalDateTime.now().plusDays(2)).dueAt(LocalDateTime.now().plusDays(23 - index))
                .pointsPossible(100).status(AssignmentStatus.NOT_STARTED)
                .build()));

        return assignments;
    }

    // ---------------------------------------------------------------- grades

    private void seedGrades(Course course, List<Assignment> assignments, int index) {
        Assignment lab1 = assignments.get(0);
        Assignment lab2 = assignments.get(1);
        Assignment lab3 = assignments.get(2);
        Assignment assessment1 = assignments.get(3);
        Assignment assessment2 = assignments.get(4);

        GradeCategory practice = GradeCategory.builder()
                .course(course).name("Practice").weightPercent(10).orderIndex(0).build();
        List<GradeItem> practiceItems = new ArrayList<>();
        practiceItems.add(GradeItem.builder().category(practice).assignment(lab1)
                .name("Practice Lab 1").maxScore(5).scoreEarned(4.5).posted(true).orderIndex(0).build());
        practiceItems.add(GradeItem.builder().category(practice).assignment(lab2)
                .name("Practice Lab 2").maxScore(5).posted(false).orderIndex(1).build());
        practiceItems.add(GradeItem.builder().category(practice).assignment(lab3)
                .name("Practice Lab 3").maxScore(5).posted(false).orderIndex(2).build());
        practice.setItems(practiceItems);
        gradeCategoryRepository.save(practice);

        boolean firstAssessmentGraded = index == 0 || index == 2;
        GradeCategory assessment1Cat = GradeCategory.builder()
                .course(course).name("Assessment 1").weightPercent(45).orderIndex(1).build();
        List<GradeItem> a1Items = new ArrayList<>();
        a1Items.add(GradeItem.builder().category(assessment1Cat).assignment(assessment1)
                .name("Assessment 1").maxScore(100)
                .scoreEarned(firstAssessmentGraded ? 82.0 : null)
                .posted(firstAssessmentGraded).orderIndex(0).build());
        assessment1Cat.setItems(a1Items);
        gradeCategoryRepository.save(assessment1Cat);

        GradeCategory assessment2Cat = GradeCategory.builder()
                .course(course).name("Assessment 2").weightPercent(45).orderIndex(2).build();
        List<GradeItem> a2Items = new ArrayList<>();
        a2Items.add(GradeItem.builder().category(assessment2Cat).assignment(assessment2)
                .name("Assessment 2").maxScore(100).posted(false).orderIndex(0).build());
        assessment2Cat.setItems(a2Items);
        gradeCategoryRepository.save(assessment2Cat);
    }

    // ------------------------------------------------------------ timetable

    private void seedTimetable(Course course, CourseBlueprint bp) {
        TimetableEntry live = TimetableEntry.builder()
                .course(course).dayOfWeek(bp.day()).startTime(bp.start()).endTime(bp.end())
                .room(bp.room()).term("Current Semester").weekType(WeekType.ODD).sessionLabel("Live Session")
                .build();
        timetableEntryRepository.save(live);

        TimetableEntry office = TimetableEntry.builder()
                .course(course).dayOfWeek(bp.day()).startTime(bp.start()).endTime(bp.start().plusMinutes(30))
                .room(bp.room()).term("Current Semester").weekType(WeekType.EVEN).sessionLabel("Office Hour")
                .build();
        timetableEntryRepository.save(office);
    }

    // --------------------------------------------------------- announcements

    private void seedAnnouncements(List<CourseBlueprint> blueprints) {
        announcementRepository.save(Announcement.builder()
                .title("Welcome to the new academic term!")
                .body("We look forward to celebrating the beginning of the new academic year with you.")
                .postedAt(LocalDateTime.now().minusDays(6))
                .postedBy("StudyBox Team")
                .build());

        announcementRepository.save(Announcement.builder()
                .title("Library access renewed")
                .body("Your digital library access has been renewed for the current term.")
                .postedAt(LocalDateTime.now().minusDays(2))
                .postedBy("Student Support")
                .build());

        List<Course> courses = courseRepository.findAll();
        for (int i = 0; i < courses.size() && i < blueprints.size(); i++) {
            Course course = courses.get(i);
            announcementRepository.save(Announcement.builder()
                    .course(course)
                    .title("Assessment schedule posted")
                    .body("The assessment schedule for " + course.getTitle() + " is now available under Grades.")
                    .postedAt(LocalDateTime.now().minusDays(1 + i))
                    .postedBy(blueprints.get(i).instructors().get(0).name())
                    .build());
        }
    }

    // ------------------------------------------------------------- sessions

    private void seedClassSessions(Course course, Student student, int index) {
        SessionStatus[] pattern = {
                SessionStatus.PRESENT, SessionStatus.PRESENT, SessionStatus.PRESENT,
                SessionStatus.LATE, SessionStatus.PRESENT, SessionStatus.PRESENT,
                index == 1 ? SessionStatus.AUTHORIZED_ABSENCE : SessionStatus.PRESENT,
                index == 3 ? SessionStatus.UNAUTHORIZED_ABSENCE : SessionStatus.PRESENT,
        };
        for (int week = 0; week < pattern.length; week++) {
            ClassSession session = ClassSession.builder()
                    .course(course)
                    .student(student)
                    .sessionDate(LocalDate.now().minusWeeks(pattern.length - week))
                    .sessionLabel("Live Session " + (week + 1))
                    .status(pattern[week])
                    .hasFiles(week % 3 == 0)
                    .build();
            classSessionRepository.save(session);
        }
    }

    // ---------------------------------------------------------- discussions

    private void seedDiscussionThread(Course course, CourseBlueprint bp, int index) {
        String instructorName = bp.instructors().get(0).name();

        DiscussionThread thread = DiscussionThread.builder()
                .course(course)
                .title("Welcome thread — introduce yourself")
                .authorName(instructorName)
                .createdAt(LocalDateTime.now().minusDays(18 - index))
                .closedForComments(false)
                .build();
        discussionThreadRepository.save(thread);

        List<DiscussionPost> posts = new ArrayList<>();
        posts.add(DiscussionPost.builder()
                .thread(thread).authorName(instructorName)
                .body("Welcome to " + course.getTitle() + "! Reply here with a short introduction and what you're hoping to learn this term.")
                .postedAt(LocalDateTime.now().minusDays(18 - index))
                .build());
        posts.add(DiscussionPost.builder()
                .thread(thread).authorName("Jordan Blake")
                .body("Hi everyone, looking forward to this course — especially the hands-on labs.")
                .postedAt(LocalDateTime.now().minusDays(17 - index))
                .build());
        thread.setPosts(posts);
        discussionThreadRepository.save(thread);

        if (index == 1) {
            DiscussionThread qa = DiscussionThread.builder()
                    .course(course)
                    .title("Assessment 1 — clarifying questions")
                    .authorName("Jordan Blake")
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .closedForComments(false)
                    .build();
            discussionThreadRepository.save(qa);
            List<DiscussionPost> qaPosts = new ArrayList<>();
            qaPosts.add(DiscussionPost.builder()
                    .thread(qa).authorName("Jordan Blake")
                    .body("Does the Assessment 1 rubric expect the write-up in the same repo as the code, or a separate doc?")
                    .postedAt(LocalDateTime.now().minusDays(4))
                    .build());
            qaPosts.add(DiscussionPost.builder()
                    .thread(qa).authorName(instructorName)
                    .body("Same repo is fine — a README section works well.")
                    .postedAt(LocalDateTime.now().minusDays(3))
                    .build());
            qa.setPosts(qaPosts);
            discussionThreadRepository.save(qa);
        }
    }

    // ------------------------------------------------------------ classmates

    private List<Student> seedClassmates() {
        String[] names = {
                "Amara Osei", "Liam Chen", "Noor Haidari", "Sofia Marín", "Kwame Boateng",
                "Yuki Tanaka", "Elif Yılmaz", "Diego Fernández", "Priya Sharma", "Lucas Silva",
        };
        List<Student> classmates = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Student classmate = Student.builder()
                    .studentNumber("ST-" + (100300 + i))
                    .fullName(names[i])
                    .email(names[i].toLowerCase().replaceAll("[^a-z]+", ".") + "@studybox.app")
                    .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .programme("Bachelor of Science (BSc) (Honours) in Computer Science")
                    .firstLoginDate(LocalDate.now().minusMonths(6))
                    .build();
            classmates.add(studentRepository.save(classmate));
        }
        return classmates;
    }

    private void seedClassmateEnrollments(List<Student> classmates, List<Course> courses) {
        for (int i = 0; i < classmates.size(); i++) {
            int courseCount = 2 + (i % 4);
            for (int c = 0; c < courses.size(); c++) {
                if ((c + i) % courses.size() >= courseCount) continue;
                Enrollment enrollment = Enrollment.builder()
                        .student(classmates.get(i))
                        .course(courses.get(c))
                        .presences(8 + ((i * 3 + c) % 6))
                        .authorizedAbsences((i + c) % 5 == 0 ? 1 : 0)
                        .unauthorizedAbsences((i + c) % 7 == 0 ? 1 : 0)
                        .lates((i + c) % 4 == 0 ? 1 : 0)
                        .build();
                enrollmentRepository.save(enrollment);
            }
        }
    }

    // ------------------------------------------------------------- financial

    private void seedFinancial(Student student) {
        installmentRepository.save(Installment.builder()
                .student(student).term("Current Semester").installmentNumber(1)
                .dueDate(LocalDate.now().minusDays(30)).amount(2500.0).status(InstallmentStatus.PAID)
                .build());
        installmentRepository.save(Installment.builder()
                .student(student).term("Current Semester").installmentNumber(2)
                .dueDate(LocalDate.now().plusDays(15)).amount(2500.0).status(InstallmentStatus.UNPAID)
                .build());
        installmentRepository.save(Installment.builder()
                .student(student).term("Current Semester").installmentNumber(3)
                .dueDate(LocalDate.now().plusDays(75)).amount(2500.0).status(InstallmentStatus.UNPAID)
                .build());

        invoiceRepository.save(Invoice.builder()
                .student(student).invoiceNumber("INV-2026-0091")
                .description("Tuition — Current Semester, Installment 1")
                .issueDate(LocalDate.now().minusDays(32)).dueDate(LocalDate.now().minusDays(30))
                .amount(2500.0).status(InvoiceStatus.PAID)
                .build());
        invoiceRepository.save(Invoice.builder()
                .student(student).invoiceNumber("INV-2026-0114")
                .description("Tuition — Current Semester, Installment 2")
                .issueDate(LocalDate.now().minusDays(2)).dueDate(LocalDate.now().plusDays(15))
                .amount(2500.0).status(InvoiceStatus.UNPAID)
                .build());
        invoiceRepository.save(Invoice.builder()
                .student(student).invoiceNumber("INV-2026-0022")
                .description("Student services fee")
                .issueDate(LocalDate.now().minusDays(60)).dueDate(LocalDate.now().minusDays(45))
                .amount(150.0).status(InvoiceStatus.PAID)
                .build());
    }

    // ----------------------------------------------------------- catalog

    private void seedCatalog() {
        record CatalogBlueprint(String code, String title, int credits, String category, String description) {
        }
        List<CatalogBlueprint> items = List.of(
                new CatalogBlueprint("COMP-6001", "Data Structures & Algorithms", 6, "Core", "Fundamental data structures, complexity analysis, and algorithm design."),
                new CatalogBlueprint("COMP-6002", "Databases", 6, "Core", "Relational modelling, SQL, and an introduction to NoSQL stores."),
                new CatalogBlueprint("COMP-6010", "Artificial Intelligence Foundations", 6, "Elective", "Search, knowledge representation, and an introduction to machine learning."),
                new CatalogBlueprint("COMP-6011", "DevOps & Site Reliability", 6, "Elective", "CI/CD pipelines, observability, and reliability engineering practices."),
                new CatalogBlueprint("DES-4003", "UX/UI Design Principles", 4, "Elective", "Human-centred design, prototyping, and usability evaluation."),
                new CatalogBlueprint("COMP-7099", "Capstone Project I", 12, "Capstone", "A supervised, term-long software project with a faculty advisor.")
        );
        for (CatalogBlueprint item : items) {
            catalogCourseRepository.save(CatalogCourse.builder()
                    .code(item.code()).title(item.title()).credits(item.credits())
                    .term("Next Semester").category(item.category()).description(item.description())
                    .build());
        }
    }

    // ---------------------------------------------------------- messaging

    private void seedMessaging(Student student, List<CourseBlueprint> blueprints) {
        String instructorName = blueprints.get(1).instructors().get(0).name();

        Conversation withInstructor = Conversation.builder()
                .student(student).subject("Question about Assessment 1 rubric")
                .withName(instructorName).build();
        List<Message> m1 = new ArrayList<>();
        m1.add(Message.builder().conversation(withInstructor).senderName(student.getFullName()).fromStudent(true)
                .body("Hi, could you clarify how the Assessment 1 write-up should be submitted?")
                .sentAt(LocalDateTime.now().minusDays(5)).read(true).build());
        m1.add(Message.builder().conversation(withInstructor).senderName(instructorName).fromStudent(false)
                .body("Hi Jordan — a README section in the same repo works well. Let me know if you have more questions!")
                .sentAt(LocalDateTime.now().minusDays(4)).read(false).build());
        withInstructor.setMessages(m1);
        conversationRepository.save(withInstructor);

        Conversation withSupport = Conversation.builder()
                .student(student).subject("Library access renewal").withName("Student Support").build();
        List<Message> m2 = new ArrayList<>();
        m2.add(Message.builder().conversation(withSupport).senderName("Student Support").fromStudent(false)
                .body("Your digital library access has been renewed for the current term.")
                .sentAt(LocalDateTime.now().minusDays(2)).read(true).build());
        withSupport.setMessages(m2);
        conversationRepository.save(withSupport);

        Conversation systemReminder = Conversation.builder()
                .student(student).subject("Reminder: no submission for Practice Lab 3")
                .withName("StudyBox System").build();
        List<Message> m3 = new ArrayList<>();
        m3.add(Message.builder().conversation(systemReminder).senderName("StudyBox System").fromStudent(false)
                .body("We haven't seen a submission for Practice Lab 3 yet. It's due soon — check the Assignments tab.")
                .sentAt(LocalDateTime.now().minusHours(20)).read(false).build());
        systemReminder.setMessages(m3);
        conversationRepository.save(systemReminder);
    }

    // -------------------------------------------------------------- files

    private void seedFiles(Student student, List<Course> courses) {
        fileResourceRepository.save(FileResource.builder()
                .student(student).category(FileCategory.PERSONAL)
                .fileName("CV_Jordan_Blake.pdf").fileType("PDF").sizeLabel("214 KB")
                .uploadedAt(LocalDateTime.now().minusDays(40)).build());
        fileResourceRepository.save(FileResource.builder()
                .student(student).category(FileCategory.PERSONAL)
                .fileName("Enrollment_Confirmation.pdf").fileType("PDF").sizeLabel("88 KB")
                .uploadedAt(LocalDateTime.now().minusMonths(6)).build());

        String[][] courseFiles = {
                {"Syllabus.pdf", "PDF", "312 KB"},
                {"Week 2 Slides.pptx", "PPTX", "4.1 MB"},
        };
        for (Course course : courses) {
            for (String[] cf : courseFiles) {
                fileResourceRepository.save(FileResource.builder()
                        .student(student).course(course).category(FileCategory.COURSE)
                        .fileName(cf[0]).fileType(cf[1]).sizeLabel(cf[2])
                        .uploadedAt(LocalDateTime.now().minusDays(20)).build());
            }
        }
    }
}
