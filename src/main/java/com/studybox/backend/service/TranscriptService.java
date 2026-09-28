package com.studybox.backend.service;

import com.studybox.backend.domain.Student;
import com.studybox.backend.dto.GradeCourseResponse;
import com.studybox.backend.exception.NotFoundException;
import com.studybox.backend.repository.StudentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Renders a simple "Unofficial Transcript" PDF from the seeded demo grade data —
 *  mirrors Classter's Reporting tab. Sample data only, not an official document. */
@Service
public class TranscriptService {

    private final StudentRepository studentRepository;
    private final GradeService gradeService;

    public TranscriptService(StudentRepository studentRepository, GradeService gradeService) {
        this.studentRepository = studentRepository;
        this.gradeService = gradeService;
    }

    @Transactional(readOnly = true)
    public byte[] generateUnofficialTranscript(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        List<GradeCourseResponse> courses = gradeService.listForStudent(studentId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bodyBoldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float margin = 56;
            float y = page.getMediaBox().getHeight() - margin;
            float lineHeight = 16;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                y = writeLine(cs, titleFont, 18, margin, y, "StudyBox — Unofficial Transcript");
                y -= 6;
                y = writeLine(cs, bodyFont, 9, margin, y, "This is a sample document generated from demo data. Not an official academic record.");
                y -= lineHeight;

                y = writeLine(cs, bodyBoldFont, 11, margin, y, "Student: " + student.getFullName());
                y = writeLine(cs, bodyFont, 11, margin, y, "Student number: " + student.getStudentNumber());
                y = writeLine(cs, bodyFont, 11, margin, y, "Programme: " + nullToDash(student.getProgramme()));
                y = writeLine(cs, bodyFont, 11, margin, y, "Generated: " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                y -= lineHeight;

                y = writeLine(cs, bodyBoldFont, 12, margin, y, "Current term grades");
                y -= 4;

                float col1 = margin;
                float col2 = margin + 90;
                float col3 = margin + 330;
                float col4 = margin + 400;

                y = writeRow(cs, bodyBoldFont, 10, y, col1, col2, col3, col4, "Code", "Course", "Credits", "Grade");
                y -= 2;
                y = writeLine(cs, bodyFont, 10, margin, y, "----------------------------------------------------------------------");

                double totalWeighted = 0;
                int gradedCourses = 0;

                for (GradeCourseResponse course : courses) {
                    String grade = course.totalPercent() != null ? course.totalPercent() + "%" : "In progress";
                    y = writeRow(cs, bodyFont, 10, y, col1, col2, col3, col4,
                            course.courseCode(), truncate(course.courseTitle(), 34), "6", grade);
                    if (course.totalPercent() != null) {
                        totalWeighted += course.totalPercent();
                        gradedCourses++;
                    }
                    if (y < margin + 80) {
                        break; // single-page demo document
                    }
                }

                y -= lineHeight;
                String average = gradedCourses > 0
                        ? String.format("%.1f%%", totalWeighted / gradedCourses)
                        : "—";
                y = writeLine(cs, bodyBoldFont, 11, margin, y, "Current average across graded courses: " + average);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate transcript PDF", e);
        }
    }

    private float writeLine(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - (size + 6);
    }

    private float writeRow(PDPageContentStream cs, PDType1Font font, float size, float y,
                            float col1, float col2, float col3, float col4, String a, String b, String c, String d) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(col1, y);
        cs.showText(a);
        cs.endText();

        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(col2, y);
        cs.showText(b);
        cs.endText();

        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(col3, y);
        cs.showText(c);
        cs.endText();

        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(col4, y);
        cs.showText(d);
        cs.endText();

        return y - (size + 8);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
