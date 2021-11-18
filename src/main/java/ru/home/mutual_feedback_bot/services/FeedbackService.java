package ru.home.mutual_feedback_bot.services;

import org.springframework.stereotype.Service;
import ru.home.mutual_feedback_bot.entities.Feedback;
import ru.home.mutual_feedback_bot.repositories.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void createFeedback(Feedback user) {
        feedbackRepository.save(user);
    }
}
