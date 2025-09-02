package com.livk.mongo;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author livk
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public HttpEntity<List<User>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    public HttpEntity<User> save(@RequestBody User user) {
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("/{id}")
    public HttpEntity<User> update(@PathVariable ObjectId id, @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @GetMapping("/query")
    public HttpEntity<User> findByName(@RequestParam String name) {
        return ResponseEntity.ok(userService.findByName(name));
    }

    @DeleteMapping("/{id}")
    public HttpEntity<Void> remove(@PathVariable ObjectId id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
