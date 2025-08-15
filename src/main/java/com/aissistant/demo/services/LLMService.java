package com.aissistant.demo.services;

import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LLMService {

    /**
     * Extract tags from ticket description + headline
     * Placeholder: in real implementation, call your LLM or embeddings model
     */
    public List<String> extractTags(String text) {
        // Very simple demo logic, replace with real LLM call
        List<String> tags = new ArrayList<>();
        if (text.toLowerCase().contains("internet") || text.toLowerCase().contains("connection")) tags.add("network");
        if (text.toLowerCase().contains("registration") || text.toLowerCase().contains("form")) tags.add("code");
        if (text.toLowerCase().contains("printer")) tags.add("hardware");
        return tags;
    }

    /**
     * Rank users by semantic match between ticket and users
     * Placeholder: in real implementation, use GPT embeddings or LLM reasoning
     */
    public List<User> rankUsersByMatch(Ticket ticket, List<User> candidates) {
        // Simple demo: sort by number of matching tags in candidate expertise
        return candidates.stream()
                .sorted((u1, u2) -> {
                    long m1 = u1.getExpertiseTags().keySet().stream()
                            .filter(ticket.getTags()::contains).count();
                    long m2 = u2.getExpertiseTags().keySet().stream()
                            .filter(ticket.getTags()::contains).count();
                    return Long.compare(m2, m1); // descending
                })
                .collect(Collectors.toList());
    }
}
