package com.livk.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author livk
 */
@Repository
public interface UserMongoRepository extends MongoRepository<User, ObjectId> {

	User findByName(String name);

}
