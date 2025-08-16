package com.aissistant.demo.services.LLMService;

import com.aissistant.demo.models.Ticket;
import com.aissistant.demo.models.User;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Service
public class LLMService {

    private Client client;
    private String prompt1;
    private String prompt2;

    public LLMService(Client client) {
        this.client = client;
        this.prompt1 = getPrompt("GetTags.txt");
        this.prompt2 = getPrompt("GetUserRankings.txt");
    }
    /**
     * Extract tags from ticket description + headline
     * */
    public List<String> extractTags(String text) {
        // Very simple demo logic, replace with real LLM call
        List<String> tags = new ArrayList<>();
        text = normalizeText(text);
        if (text.equals("")){
            return tags;
        }

        System.out.println(this.prompt1);
        GenerateContentResponse response =client.models.generateContent(
                "gemini-2.5-flash",
                this.prompt1 + text,
                null);
        tags = List.of(response.text().split(","));
        for (String tag : tags) {
            if (tag.charAt(0) == ' '){
                tag = tag.substring(1); // remove leading space
            }
            if(tag.charAt(tag.length() - 1) == ' '){
                tag = tag.substring(0, tag.length() - 1); // remove trailing space
            }
        }
        return tags;
    }

    /**
     * Rank users by semantic match between ticket and users
     * Placeholder: in real implementation, use GPT embeddings or LLM reasoning
     */
    public List<User> rankUsersByMatch(Ticket ticket, List<User> candidates) {
        List<User> usersByRanking = new ArrayList<>();

        usersByRanking = candidates.stream()
                .map(user -> {

                    UserMatch match = new UserMatch();
                    match.setUser(user);

                    double score = 0;
                    String userText = normalizeText(user.getName() + " " + user.getBio());
                    GenerateContentResponse response = client.models.generateContent(
                            "gemini-2.5-flash",
                            this.prompt2 + ticket.getHeadline() + " " + ticket.getDescription() + "\n" + userText,
                            null);

                    if ( response.text() != null && !response.text().isEmpty()) {
                        try{
                            score = Double.parseDouble(response.text());
                        }catch(Exception e){
                            score = 0;
                        }
                    }
                    match.setScore(score);
                    return match;
                })
                .filter(match -> match.score > 0)
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Sort by score descending
                .map(match -> match.user)
                .toList();
        return usersByRanking;
    }


    private String normalizeText(String text){
        if (text == null) return "";

        text = text.toLowerCase();

        // Normalize whitespace (collapse multiple spaces/newlines)
        text = text.replaceAll("\\s+", " ").trim();

        // Normalize quotes/dashes
        text = text.replaceAll("[“”]", "\"");
        text = text.replaceAll("[‘’]", "'");
        text = text.replaceAll("[–—]", "-");

        return text;
    }
    private String getPrompt(String path) {
        try {
            Resource resource = new ClassPathResource("llm_prompts/" + path);


            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to load prompt: " + path + " - " + e.getMessage());
            return "";
        }
    }
    @Data
    private class UserMatch {
        User user;
        double score;
    }
}
