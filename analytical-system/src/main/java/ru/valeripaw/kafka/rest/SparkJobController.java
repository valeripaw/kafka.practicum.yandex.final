package ru.valeripaw.kafka.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.valeripaw.kafka.service.SparkAnalyticsJob;

@RestController
@RequestMapping("/api/spark")
@RequiredArgsConstructor
public class SparkJobController {

    private final SparkAnalyticsJob sparkAnalyticsJob;

    @PostMapping("/run")
    public ResponseEntity<String> runJob() {
        try {
            sparkAnalyticsJob.runJob();
            return ResponseEntity.ok("Spark job started successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error running Spark job: " + e.getMessage());
        }
    }

}
