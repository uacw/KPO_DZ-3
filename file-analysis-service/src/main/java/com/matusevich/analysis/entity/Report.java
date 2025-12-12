package com.matusevich.analysis.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID workId;
    private boolean isPlagiarism;
    private int score;
    private LocalDateTime checkDate;

    public Report() {
    }

    public Report(UUID workId, boolean isPlagiarism, int score) {
        this.workId = workId;
        this.isPlagiarism = isPlagiarism;
        this.score = score;
        this.checkDate = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWorkId() { return workId; }
    public void setWorkId(UUID workId) { this.workId = workId; }

    public boolean isPlagiarism() { return isPlagiarism; }
    public void setPlagiarism(boolean plagiarism) { isPlagiarism = plagiarism; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDateTime checkDate) { this.checkDate = checkDate; }
}
