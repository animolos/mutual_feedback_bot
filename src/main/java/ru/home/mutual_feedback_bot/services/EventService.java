package ru.home.mutual_feedback_bot.services;

import org.springframework.stereotype.Service;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.repositories.EventRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event findById(Long eventId){
        return eventRepository.findById(eventId).orElse(null);
    }
}
