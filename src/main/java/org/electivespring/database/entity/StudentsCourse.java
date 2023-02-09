package org.electivespring.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.sql.Date;
@Entity
@Table(name = "students_course")
public class StudentsCourse implements Serializable {
    @Column(name = "student_id")
    int studentId;
    @Id
    @Column(name = "course_id")
    int courseId;
    @Column(name = "registration_date")
    Date registrationDate;
    @Column
    Integer grade;

    public StudentsCourse() {
    }

    public StudentsCourse(int studentId, int courseId, Date registrationDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.registrationDate = registrationDate;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getId() {
        return courseId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "StudentsCourse{" +
                "studentId=" + studentId +
                ", courseId=" + courseId +
                ", registrationDate=" + registrationDate +
                ", grade=" + grade +
                '}';
    }
}