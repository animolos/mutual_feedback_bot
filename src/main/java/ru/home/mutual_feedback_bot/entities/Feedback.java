package ru.home.mutual_feedback_bot.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @NonNull
    private String message;

    @ManyToOne
    @JoinColumn(name = "destination")
    @NonNull
    private Event destination;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    @NonNull
    private User createdBy;
}
