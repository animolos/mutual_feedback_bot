//package ru.home.mutual_feedback_bot.entities;
//
//import javax.persistence.*;
//import java.util.Set;
//
//@Entity
//@Table(name = "event")
//public class Event {
//    @Id
//    private int id;
//
//    @Column(name = "name")
//    private String name;
//
//    @OneToOne()
//    private User createdBy;
//
//    @Column(name = "description")
//    private String description;
//
//    @ManyToMany(mappedBy = "events")
//    private Set<User> users;
//
//    @OneToMany()
//    private Set<Feedback> feedbacks;
//}
