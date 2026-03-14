package ru.valeripaw.kafka.db.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.valeripaw.kafka.db.client.entity.ClientRequest;

@Repository
public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {

}
