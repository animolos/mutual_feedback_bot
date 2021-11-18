package ru.home.mutual_feedback_bot.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @NonNull
    private String name;

    @Column
    @NonNull
    private String description;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User createdBy;

    @OneToMany(mappedBy="destination")
    private Set<Feedback> feedbacks;
}
