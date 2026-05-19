package lk.freelance.backend.controller;

//This is made for testing without jwt. for testing only. nothing to do with the project itself

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private List<String> tasks = new ArrayList<>();

    // GET all tasks
    @GetMapping
    public List<String> getTasks() {
        return tasks;
    }

    // POST new task
    @PostMapping
    public String addTask(@RequestBody String task) {
        tasks.add(task);
        return "Task added: " + task;
    }

    // PUT update task
    @PutMapping("/{index}")
    public String updateTask(@PathVariable int index,
                             @RequestBody String newTask) {

        if (index >= tasks.size()) {
            return "Task not found";
        }

        tasks.set(index, newTask);

        return "Task updated";
    }

    // DELETE task
    @DeleteMapping("/{index}")
    public String deleteTask(@PathVariable int index) {

        if (index >= tasks.size()) {
            return "Task not found";
        }

        tasks.remove(index);

        return "Task deleted";
    }
}