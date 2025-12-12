package com.matusevich.storing.service;

import com.matusevich.storing.entity.Submission;
import com.matusevich.storing.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class SubmissionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository repository;

    public SubmissionService(SubmissionRepository repository) {
        this.repository = repository;
    }

    @Value("${file.storage.path}")
    private String uploadPath;

    public Submission saveSubmission(String studentName, String taskName, MultipartFile file) {
        try {
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            
            // Генерируем уникальное имя файла
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = root.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            Submission submission = new Submission(studentName, taskName, filePath.toString());
            log.info("Сохранение работы для студента: {}", studentName);
            return repository.save(submission);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    public Submission getSubmission(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new com.matusevich.storing.exception.EntityNotFoundException("Работа не найдена: " + id));
    }

    public List<Submission> getSubmissionsByTask(String taskName) {
        return repository.findByTaskName(taskName);
    }
}
