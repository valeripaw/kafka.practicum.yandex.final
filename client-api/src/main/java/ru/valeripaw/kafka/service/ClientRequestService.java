package ru.valeripaw.kafka.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeripaw.kafka.db.client.entity.ClientRequest;
import ru.valeripaw.kafka.db.client.repository.ClientRequestRepository;

import java.time.LocalDateTime;

@Service
public class ClientRequestService {

    private final ClientRequestRepository repository;

    public ClientRequestService(ClientRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional("clientTransactionManager")
    public ClientRequest saveSearch(String query) {
        ClientRequest request = new ClientRequest();
        request.setType("SEARCH_PRODUCT_REQUEST");
        request.setQuery(query);
        request.setCreatedAt(LocalDateTime.now());
        return save(request);
    }

    @Transactional("clientTransactionManager")
    public ClientRequest saveRecommendation(String query) {
        ClientRequest request = new ClientRequest();
        request.setType("RECOMMENDATION_REQUEST");
        request.setQuery(query);
        request.setCreatedAt(LocalDateTime.now());
        return save(request);
    }

    private ClientRequest save(ClientRequest clientRequest) {
        return repository.save(clientRequest);
    }

}
