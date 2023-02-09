package org.electivespring.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

import static org.electivespring.database.entity.StudentsSubtopic.completion.UNCOMPLETED;
@Entity
@Table(name = "students_subtopic")
public class StudentsSubtopic implements Serializable {
    @Column(name = "student_id")
    int studentId;
    @Id
    @Column(name = "subtopic_id")
    int subtopicId;
    @Column
    String completion;

    public StudentsSubtopic(int studentId, int subtopicId) {
        this.studentId = studentId;
        this.subtopicId = subtopicId;
        this.completion = UNCOMPLETED.toString();
    }

    public StudentsSubtopic() {
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubtopicId() {
        return subtopicId;
    }

    public void setSubtopicId(int subtopicId) {
        this.subtopicId = subtopicId;
    }

    public String getCompletion() {
        return completion;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
    }

    @Override
    public String toString() {
        return "StudentsSubtopic{" +
                "studentId=" + studentId +
                ", subtopicId=" + subtopicId +
                ", completion='" + completion + '\'' +
                '}';
    }

    public enum completion {

        UNCOMPLETED("uncompleted"),
        COMPLETED("completed");

        private final String title;

        completion(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
