package com.aissistant.demo.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aissistant.demo.models.ERole;
import com.aissistant.demo.models.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
