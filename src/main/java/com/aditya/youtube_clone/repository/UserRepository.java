package com.aditya.youtube_clone.repository;

import com.aditya.youtube_clone.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

}
