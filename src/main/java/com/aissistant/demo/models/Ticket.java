package com.aissistant.demo.models;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "tickets")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    private ObjectId id;

    @NotNull
    private String headline;

    @NotNull
    private String description;

    private Date date;

    @DBRef
    private User issuer;

    @DBRef
    private User solver;

    private boolean isSolved = false; // new field

    private List<String> tags; // ["network", "hardware", ...]

    public Ticket(String headline, String description) {
        this.headline = headline;
        this.description = description;
        this.date = new Date();
    }

    // getters and setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getIssuer() {
        return issuer;
    }

    public void setIssuer(User issuer) {
        this.issuer = issuer;
    }

    @Nullable
    public User getSolver() {
        return solver;
    }

    public void setSolver(@Nullable User solver) {
        this.solver = solver;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }
}
