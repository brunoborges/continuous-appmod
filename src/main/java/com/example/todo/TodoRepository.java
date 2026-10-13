package com.example.todo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOrderByIdAsc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Todo t SET t.completed = CASE WHEN t.completed = true THEN false ELSE true END WHERE t.id = :id")
    int toggleCompletionById(@Param("id") long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Todo t WHERE t.id = :id")
    int deleteTodoById(@Param("id") long id);
}
