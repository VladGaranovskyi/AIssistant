package com.aissistant.demo.repositories;

import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, ObjectId> {
    Optional<Ticket> findByIssuer(User issuer);


}
