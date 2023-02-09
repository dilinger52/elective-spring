package org.electivespring.database.entity;

import jakarta.persistence.*;

import java.io.Serializable;
@Entity
@Table(name = "course_subtopics")
public class Subtopic implements Serializable {
    @Column(name = "course_id")
    int courseId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int subtopicId;
    @Column(name = "subtopic_name")
    String subtopicName;
    @Column(name = "subtopic_content")
    String subtopicContent;

    public Subtopic(int courseId, String subtopicName) {
        this.courseId = courseId;
        this.subtopicId = 0;
        this.subtopicName = subtopicName;
    }

    public Subtopic() {

    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getSubtopicId() {
        return subtopicId;
    }

    public void setSubtopicId(int subtopicId) {
        this.subtopicId = subtopicId;
    }

    public String getSubtopicName() {
        return subtopicName;
    }

    public void setSubtopicName(String subtopicName) {
        this.subtopicName = subtopicName;
    }

    public String getSubtopicContent() {
        return subtopicContent;
    }

    public void setSubtopicContent(String subtopicContent) {
        this.subtopicContent = subtopicContent;
    }

    @Override
    public String toString() {
        return "Subtopic{" +
                "courseId=" + courseId +
                ", subtopicId=" + subtopicId +
                ", subtopicName='" + subtopicName + '\'' +
                '}';
    }
}
