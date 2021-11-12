package ru.home.mutual_feedback_bot.entities;

import javax.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    private long id;

    @Column(name = "message")
    private String message;

    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "destination", referencedColumnName = "id")
    private Event destination;

    @OneToOne()
    private User createdBy;
}
