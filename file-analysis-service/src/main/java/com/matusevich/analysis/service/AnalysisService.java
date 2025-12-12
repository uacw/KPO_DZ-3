package com.matusevich.analysis.service;

import com.matusevich.analysis.client.StoringServiceClient;
import com.matusevich.analysis.dto.SubmissionDto;
import com.matusevich.analysis.entity.Report;
import com.matusevich.analysis.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalysisService.class);

    private final StoringServiceClient storingClient;
    private final ReportRepository repository;
    private final com.matusevich.analysis.client.QuickChartClient quickChartClient;

    public AnalysisService(StoringServiceClient storingClient, ReportRepository repository, com.matusevich.analysis.client.QuickChartClient quickChartClient) {
        this.storingClient = storingClient;
        this.repository = repository;
        this.quickChartClient = quickChartClient;
    }

    public Report analyze(UUID workId) {
        // 1. Получаем информацию о работе
        SubmissionDto currentSubmission = storingClient.getSubmission(workId);
        
        // 2. Получаем все работы по этому заданию
        List<SubmissionDto> allSubmissions = storingClient.getSubmissionsByTask(currentSubmission.getTaskName());

        // 3. Алгоритм проверки (Full Content Comparison)
        boolean isPlagiarism = false;
        int score = 0;
        
        // Скачиваем контент текущей работы
        byte[] currentContent = storingClient.downloadFile(workId);

        for (SubmissionDto sub : allSubmissions) {
            // Игнорируем саму себя
            if (sub.getId().equals(currentSubmission.getId())) continue;
            
            // Проверка только если имена студентов разные
            if (!sub.getStudentName().equals(currentSubmission.getStudentName())) {
                try {
                    // Тянем файл "соседа" из хранилища
                    byte[] otherContent = storingClient.downloadFile(sub.getId());
                    
                    // Сравниваем байт-в-байт
                    if (java.util.Arrays.equals(currentContent, otherContent)) {
                        // Если содержимое совпадает, проверяем, кто сдал позже
                        // Если МЫ сдали позже — значит МЫ списали.
                        if (currentSubmission.getSubmissionDate().isAfter(sub.getSubmissionDate())) {
                            isPlagiarism = true;
                            score = 100;
                            log.info("Опа)) Плагиат: работа {} списана с более ранней работы {}", workId, sub.getId());
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Не смогли скачать файл {} для сравнения, пропускаем...", sub.getId());
                    continue;
                }
            }
        }
        
        // Удаляем старый отчет, если он был (чтобы не дублировать)
        repository.findByWorkId(workId).ifPresent(repository::delete);

        Report report = new Report(workId, isPlagiarism, score);
        return repository.save(report);
    }

    public Report getReport(UUID workId) {
        return repository.findByWorkId(workId)
                .orElseThrow(() -> new com.matusevich.analysis.exception.EntityNotFoundException("Отчет не найден:" + workId));
    }

    public byte[] generateWordCloud(UUID workId) {
        // 1. Скачиваем файл
        byte[] content = storingClient.downloadFile(workId);
        
        // 2. Превращаем в текст
        String text = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        
        // 3. Формируем запрос к QuickChart
        // Ограничиваем текст, чтобы не сломать внешний API слишком длинным URL/Body
        if (text.length() > 5000) {
            text = text.substring(0, 5000);
        }
        
        com.matusevich.analysis.dto.QuickChartRequest request = new com.matusevich.analysis.dto.QuickChartRequest(text);
        
        // 4. Получаем картинку
        try {
            return quickChartClient.generateWordCloud(request);
        } catch (Exception e) {
            log.error("Ошибка генерации облака слов для {}: {}", workId, e.getMessage());
            throw new RuntimeException("Не удалось создать облако слов: " + e.getMessage());
        }
    }
}
