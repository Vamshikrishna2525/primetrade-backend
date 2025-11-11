package com.primetrade.repository;

import com.primetrade.model.Task;
import com.primetrade.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
}