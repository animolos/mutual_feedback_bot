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

    public void insertOrUpdate(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    public Feedback findById(Long feedbackId){
        return feedbackRepository.findById(feedbackId).orElse(null);
    }
}
