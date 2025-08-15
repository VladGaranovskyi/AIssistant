package com.aissistant.demo.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class AddPersonalDataRequest {
    @NotNull
    private String name;

    @NotNull
    private String bio;

    @NotNull
    private Map<String, Double> expertiseTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Map<String, Double> getExpertiseTags() {
        return expertiseTags;
    }

    public void setExpertiseTags(Map<String, Double> expertiseTags) {
        this.expertiseTags = expertiseTags;
    }
}
