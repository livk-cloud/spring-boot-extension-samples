package com.livk.mongo;

import org.bson.types.ObjectId;

import java.util.List;

/**
 * @author livk
 */
public interface UserService {
    List<User> findAll();

    User save(User user);

    User update(ObjectId id, User user);

    void delete(ObjectId id);

    User findByName(String name);
}
