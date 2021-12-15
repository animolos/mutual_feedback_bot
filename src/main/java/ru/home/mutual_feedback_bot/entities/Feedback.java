package ru.home.mutual_feedback_bot.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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

    @Column
    @NonNull
    private Date createdAt;

    @Setter
    @ManyToOne
    @JoinColumn(name = "parentFeedback")
    private Feedback parentFeedback;

    @Setter
    @OneToMany(mappedBy = "parentFeedback")
    private Set<Feedback> childFeedback;

    public boolean isReply() {
        return this.parentFeedback != null;
    }
}
