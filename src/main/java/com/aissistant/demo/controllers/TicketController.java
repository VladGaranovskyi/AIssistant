package com.aissistant.demo.controllers;

import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import com.aissistant.demo.payload.request.TicketCreationRequest;
import com.aissistant.demo.payload.response.MessageResponse;
import com.aissistant.demo.repositories.UserRepository;
import com.aissistant.demo.services.Auth.AuthService;
import com.aissistant.demo.services.Ticket.TicketMatcher;
import com.aissistant.demo.services.Ticket.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMatcher ticketMatcher;

    @Autowired
    private AuthService authService;

    // Create ticket
    @PostMapping("/create")
    public ResponseEntity<?> createTicket(HttpServletRequest request, @Valid @RequestBody TicketCreationRequest ticketRequest) {

        Optional<User> issuer = authService.getUserDataFromCookies(request);

        if (issuer.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to create a ticket");
        }

        Ticket ticket = ticketService.createTicket(
                ticketRequest.getHeadline(),
                ticketRequest.getDescription(),
                issuer.get()
        );


        return ResponseEntity.ok(ticket);
    }

    // Get all tickets
    @GetMapping("/")
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Mark solved/unsolved
    @PostMapping("/{id}/solved")
    public Ticket markSolved(@PathVariable String id, @RequestParam boolean solved) {
        Ticket ticket = ticketService.markTicketSolved(new ObjectId(id), solved);
        ticketMatcher.updateSolverExpertise(ticket, solved);
        return ticket;
    }

    // Get top solvers for a ticket
    @GetMapping("/{id}/top-solvers")
    public List<User> getTopSolvers(@PathVariable String id) {
        Ticket ticket = ticketService.getTicketById(new ObjectId(id));
        return ticketMatcher.findTopSolvers(ticket, 5);
    }
}
