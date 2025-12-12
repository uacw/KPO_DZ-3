package com.matusevich.analysis.controller;

import com.matusevich.analysis.entity.Report;
import com.matusevich.analysis.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Анализ на плагиат", description = "API для запуска проверки и получения отчетов")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @Operation(summary = "Запустить анализ работы", description = "Инициирует проверку работы на плагиат. Сравнивает с другими работами по той же задаче.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Анализ завершен успешно"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Работа не найдена или ошибка при скачивании")
    })
    @PostMapping("/analyze/{workId}")
    public ResponseEntity<Report> analyze(
            @Parameter(description = "UUID работы, которую нужно проверить") @PathVariable UUID workId) {
        return ResponseEntity.ok(service.analyze(workId));
    }

    @Operation(summary = "Получить отчет о проверке", description = "Возвращает результат последней проверки (isPlagiarism, score).")
    @GetMapping("/reports/{workId}")
    public ResponseEntity<Report> getReport(
            @Parameter(description = "UUID работы") @PathVariable UUID workId) {
        return ResponseEntity.ok(service.getReport(workId));
    }

    @Operation(summary = "Получить облако слов", description = "Генерирует PNG-картинку с облаком слов из текста работы.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Картинка успешно сгенерирована"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Работа не найдена или ошибка генерации")
    })
    @GetMapping(value = "/word-cloud/{workId}", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getWordCloud(
            @Parameter(description = "UUID работы") @PathVariable UUID workId) {
        return ResponseEntity.ok(service.generateWordCloud(workId));
    }
}
