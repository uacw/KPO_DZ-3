package com.matusevich.analysis.client;

import com.matusevich.analysis.dto.QuickChartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "quickchart-client", url = "https://quickchart.io")
public interface QuickChartClient {

    @PostMapping(value = "/wordcloud", consumes = "application/json")
    byte[] generateWordCloud(@RequestBody QuickChartRequest request);
}
