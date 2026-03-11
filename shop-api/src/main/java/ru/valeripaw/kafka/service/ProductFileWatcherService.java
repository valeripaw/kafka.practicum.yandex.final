package ru.valeripaw.kafka.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.properties.ShopApiProperties;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFileWatcherService {

    private final ProductEventService eventService;
    private final ShopApiProperties shopApiProperties;

    @PostConstruct
    public void startWatcher() {
        new Thread(this::watchFile).start();
    }

    private void watchFile() {
        try {
            Path workingDir = Paths.get("").toAbsolutePath();
            Path productFile = workingDir.resolve(shopApiProperties.getDataFilePath()).toAbsolutePath();
            Path directory = productFile.getParent();
            String fileName = productFile.getFileName().toString();

            log.info("Watching directory: {}", directory);
            log.info("Watching file: {}", fileName);

            WatchService watchService = FileSystems.getDefault().newWatchService();
            directory.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE
            );

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (changed.equals(productFile.getFileName())) {
                        log.info("products.json was updated");
                        eventService.processFileUpdate();
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
