package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "client-api.use-command-line",
        havingValue = "true",
        matchIfMissing = false
)
public class ClientConsoleService implements CommandLineRunner {

    private static final String SEARCH = "1";
    private static final String RECOMMENDATION = "2";
    private static final String EXIT = "0";

    private final ClientRequestEngine clientRequestEngine;

    @Override
    public void run(String... args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            log.info("\nВыберите команду:");
            log.info("1 - Поиск товара");
            log.info("2 - Рекомендации");
            log.info("0 - Выход");

            String command = scanner.nextLine();

            switch (command) {
                case SEARCH:
                    log.info("Введите строку для поиска товара:");
                    String search = scanner.nextLine();
                    clientRequestEngine.search(search);
                    break;
                case RECOMMENDATION:
                    log.info("Введите запрос для рекоммендаций:");
                    String recommendation = scanner.nextLine();
                    clientRequestEngine.recommend(recommendation);
                    break;
                case EXIT:
                    System.exit(0);
                    break;
                default:
                    log.info("Неизвестная команда");
            }
        }
    }

}
