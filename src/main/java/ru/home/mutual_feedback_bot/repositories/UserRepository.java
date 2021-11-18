package ru.home.mutual_feedback_bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.home.mutual_feedback_bot.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
