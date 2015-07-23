package com.schafroth.messaging.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "people")
@NamedQueries({
        @NamedQuery(
                name = "com.example.helloworld.core.Person.findAll",
                query = "SELECT p FROM Person p"
        )
})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "payload", nullable = false)
    private String payload;
    private Long time;

    public Message() {
    }

    public Message(String fullName, Long time) {
        this.payload = fullName;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        final Message that = (Message) o;

        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.payload, that.payload) &&
                Objects.equals(this.time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payload, time);
    }
}
