package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Document
public class numberSessions {

    @Id
    private String _id;
    private int numberSessions;

    @DBRef
    private String userId;

    public numberSessions(int numberSession, String userId) {
        this.numberSessions = numberSession;
        this.userId = userId;
    }

    public void setNumberSessions(int numberSession) {
        this.numberSessions = numberSession;
    }

    public int getNumberSessions() {
        return numberSessions;
    }

    public String getUserId() {
        return userId;
    }

}
