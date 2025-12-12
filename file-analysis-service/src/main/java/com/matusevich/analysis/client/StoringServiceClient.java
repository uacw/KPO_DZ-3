package com.matusevich.analysis.client;

import com.matusevich.analysis.dto.SubmissionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "file-storing-service", url = "${file-storing-service.url:http://localhost:8081}")
public interface StoringServiceClient {

    @GetMapping("/works/{workId}")
    SubmissionDto getSubmission(@PathVariable UUID workId);

    @GetMapping("/works")
    List<SubmissionDto> getSubmissionsByTask(@RequestParam("taskName") String taskName);

    @GetMapping("/works/{id}/file")
    byte[] downloadFile(@PathVariable UUID id);
}
