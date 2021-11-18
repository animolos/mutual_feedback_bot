package ru.home.mutual_feedback_bot.services;

import org.springframework.stereotype.Service;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User insertOrUpdate(User user) {
        return userRepository.save(user);
    }

    public User findById(Long chatId){
        return userRepository.findById(chatId).orElse(null);
    }
}
