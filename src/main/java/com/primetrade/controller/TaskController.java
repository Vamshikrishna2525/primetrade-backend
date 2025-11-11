package com.primetrade.controller;

import com.primetrade.model.Task;
import com.primetrade.model.User;
import com.primetrade.repository.TaskRepository;
import com.primetrade.repository.UserRepository;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getTasks(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (user.getRole() != null && user.getRole().name().equals("ROLE_ADMIN")) {
            return ResponseEntity.ok(taskRepository.findAll());
        }
        return ResponseEntity.ok(taskRepository.findByUser(user));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        task.setUser(user);
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Task t = taskRepository.findById(id).orElseThrow();
        if (user.getRole() != null && user.getRole().name().equals("ROLE_ADMIN") || t.getUser().getId().equals(user.getId())) {
            taskRepository.deleteById(id);
            return ResponseEntity.ok("Deleted");
        }
        return ResponseEntity.status(403).body("Forbidden");
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markTaskComplete(@PathVariable Long id, Principal principal) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        Task task = taskOpt.get();

        // Optional: verify the task belongs to the logged-in user
        String username = principal.getName();
        if (!task.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to modify this task");
        }

        task.setCompleted(true);
        taskRepository.save(task);
        return ResponseEntity.ok("Task marked as completed");
    }

}