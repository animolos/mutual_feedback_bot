package ru.home.mutual_feedback_bot.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {
    @Id
    private long id;

    @Column(name = "name")
    private String name;

    @ManyToMany()
    Set<Event> events;

    @OneToMany()
    Set<Feedback> feedbacks;
}
