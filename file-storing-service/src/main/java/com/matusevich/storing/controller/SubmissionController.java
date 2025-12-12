package com.matusevich.storing.controller;

import com.matusevich.storing.entity.Submission;
import com.matusevich.storing.service.SubmissionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/works")
@Tag(name = "Загрузка и хранение работ", description = "API для загрузки, поиска и скачивания студенческих работ")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    @Operation(summary = "Загрузка работы", description = "Принимает файл и данные студента. Сохраняет метаданные в базу, а сам файл — на диск.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Файл успешно загружен"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос (неверное расширение, пустой файл)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Submission> uploadWork(
            @Parameter(description = "ФИО студента") @RequestParam("studentName") String studentName,
            @Parameter(description = "Название задачи") @RequestParam("taskName") String taskName,
            @Parameter(description = "Файл (код или текст)") @RequestParam("file") MultipartFile file) {
        
        String filename = file.getOriginalFilename();
        if (file.isEmpty()) {
            throw new com.matusevich.storing.exception.BadRequestException("Файл не может быть пустым");
        }
        
        if (filename == null || !isValidExtension(filename)) {
            // Ругаемся, если формат файла нам не подходит
            throw new com.matusevich.storing.exception.BadRequestException("Недопустимый формат файла. Мы принимаем только код (txt, java, py, etc.)");
        }
        
        return ResponseEntity.ok(service.saveSubmission(studentName, taskName, file));
    }

    private boolean isValidExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".tex") ||
               lower.endsWith(".cpp") || lower.endsWith(".java") || lower.endsWith(".cs") ||
               lower.endsWith(".py") || lower.endsWith(".kt");
    }

    @Operation(summary = "Найти работы по названию задачи", description = "Возвращает список всех загруженных работ для указанной задачи.")
    @GetMapping
    public ResponseEntity<List<Submission>> getWorksByTask(
            @Parameter(description = "Название задачи для поиска") @RequestParam("taskName") String taskName) {
        return ResponseEntity.ok(service.getSubmissionsByTask(taskName));
    }

    @Operation(summary = "Получить информацию о работе", description = "Возвращает метаданные работы по её ID.")
    @GetMapping("/{workId}")
    public ResponseEntity<Submission> getWork(
            @Parameter(description = "UUID работы") @PathVariable UUID workId) {
        return ResponseEntity.ok(service.getSubmission(workId));
    }

    @Operation(summary = "Скачать файл работы", description = "Возвращает файл работы по ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Файл найден и возвращен"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Файл или работа не найдены")
    })
    @GetMapping("/{workId}/file")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "UUID работы") @PathVariable UUID workId) {
        Submission submission = service.getSubmission(workId);
        Path path = Paths.get(submission.getFilePath());
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new com.matusevich.storing.exception.EntityNotFoundException("Файл не найден на диске");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ошибка URL файла", e);
        }
    }
}
