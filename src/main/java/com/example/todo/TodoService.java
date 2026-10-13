package com.example.todo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public Todo add(String title) {
        if (title == null || title.trim().isEmpty() || title.trim().length() > 200) {
            throw new IllegalArgumentException("Task title must contain 1 to 200 characters.");
        }
        return todoRepository.save(new Todo(title.trim()));
    }

    @Transactional
    public void toggle(long id) {
        requireUpdatedRow(todoRepository.toggleCompletionById(id));
    }

    @Transactional
    public void delete(long id) {
        requireUpdatedRow(todoRepository.deleteTodoById(id));
    }

    private void requireUpdatedRow(int updatedRows) {
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
        }
    }
}
