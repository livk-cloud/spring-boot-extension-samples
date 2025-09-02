package com.livk.mongo;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author livk
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMongoRepository userMongoRepository;

    @Override
    public List<User> findAll() {
        return userMongoRepository.findAll();
    }

    @Override
    public User save(User user) {
        return userMongoRepository.save(user);
    }

    @Override
    public User update(ObjectId id, User user) {
        user.setId(id);
        return userMongoRepository.save(user);
    }

    @Override
    public void delete(ObjectId id) {
        userMongoRepository.deleteById(id);
    }

    @Override
    public User findByName(String name) {
        return userMongoRepository.findByName(name);
    }
}
