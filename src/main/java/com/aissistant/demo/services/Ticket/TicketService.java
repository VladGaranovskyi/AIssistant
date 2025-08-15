package com.aissistant.demo.services.Ticket;

import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import com.aissistant.demo.repositories.TicketRepository;
import com.aissistant.demo.repositories.UserRepository;
import com.aissistant.demo.services.LLMService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LLMService llmService;

    public Ticket createTicket(String headline, String description, User issuer) {
        Ticket ticket = new Ticket(headline, description);
        ticket.setTags(llmService.extractTags(headline + " " + description));
        ticket.setSolved(false);
        ticket.setIssuer(issuer);
        ticket.setDate(new Date());
        Ticket freshTicket = ticketRepository.save(ticket);

        List<Ticket> prevTickets = issuer.getTickets();
        if(prevTickets == null){
            prevTickets = new ArrayList<Ticket>();
        }
        prevTickets.add(freshTicket);
        issuer.setTickets(prevTickets);

        userRepository.save(issuer);

        return freshTicket;
    }

    public Ticket getTicketById(ObjectId ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket markTicketSolved(ObjectId ticketId, boolean solved) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setSolved(solved);
        return ticketRepository.save(ticket);
    }
}

