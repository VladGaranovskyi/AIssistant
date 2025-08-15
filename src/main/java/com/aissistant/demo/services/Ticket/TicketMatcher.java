package com.aissistant.demo.services.Ticket;

import com.aissistant.demo.models.ERole;
import com.aissistant.demo.models.Role;
import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import com.aissistant.demo.services.LLMService;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketMatcher {
    private final MongoTemplate mongoTemplate;
    private final LLMService llmService;

    public TicketMatcher(MongoTemplate mongoTemplate, LLMService llmService) {
        this.mongoTemplate = mongoTemplate;
        this.llmService = llmService;
    }

    public List<User> findTopSolvers(Ticket ticket, int topN) {
        List<String> ticketTags = ticket.getTags();

        // --- Aggregation ---
        Aggregation aggregation = Aggregation.newAggregation(
                // convert map to array
                Aggregation.project()
                        .andExpression("objectToArray($expertiseTags)").as("tagsArray")
                        .andInclude("roles"), // we need roles for filtering

                Aggregation.unwind("tagsArray"),

                Aggregation.match(Criteria.where("tagsArray.k").in(ticketTags)),

                Aggregation.group("_id")
                        .sum("tagsArray.v").as("score"),

                Aggregation.sort(Sort.Direction.DESC, "score"),

                Aggregation.limit(topN)
        );

        AggregationResults<UserScore> results = mongoTemplate.aggregate(
                aggregation,
                "users",
                UserScore.class
        );

        List<String> topUserIds = results.getMappedResults().stream()
                .map(UserScore::getId)
                .collect(Collectors.toList());

        // Fetch full User objects and filter ROLE_ASSISTANT
        List<User> candidates = topUserIds.stream()
                .map(id -> mongoTemplate.findById(new ObjectId(id), User.class))
                .filter(Objects::nonNull)
                .filter(u -> u.getRoles().stream().map(Role::getName).anyMatch(r -> r == ERole.ROLE_ASSISTANT))
                .collect(Collectors.toList());

        // --- LLM Reranking ---
        candidates = llmService.rankUsersByMatch(ticket, candidates);

        return candidates;
    }

    public void assignBestSolver(Ticket ticket) {
        List<User> topSolvers = findTopSolvers(ticket, 5);
        if (!topSolvers.isEmpty()) {
            ticket.setSolver(topSolvers.get(0));
        }
    }

    public void updateSolverExpertise(Ticket ticket, boolean solved) {
        User solver = ticket.getSolver();
        if (solver == null) return;

        Map<String, Double> expertiseTags = solver.getExpertiseTags();
        for (String tag : ticket.getTags()) {
            double oldProb = expertiseTags.getOrDefault(tag, 0.5);
            double newProb = solved ? Math.min(oldProb + 0.1, 1.0) : Math.max(oldProb - 0.1, 0.0);
            expertiseTags.put(tag, newProb);
        }

        mongoTemplate.save(solver);
    }

    public static class UserScore {
        private String id;
        private double score;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
