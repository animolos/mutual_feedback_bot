package ru.home.mutual_feedback_bot.entities;

import lombok.*;
import ru.home.mutual_feedback_bot.models.ConversationStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Table(name = "users")
public class User {
    @Id
    @NonNull
    private Long id;

    @Column
    @Setter
    @NonNull
    private ConversationStatus conversationStatus = ConversationStatus.Default;

    @Column
    @Setter
    private Long selectedEventId;

    @Column
    @Setter
    private Long selectedFeedbackId;

    @OneToMany(mappedBy="createdBy")
    private final Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy="createdBy")
    private Set<Feedback> feedbacks;
}
