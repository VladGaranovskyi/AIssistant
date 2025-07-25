package com.aissistant.demo.init;

import com.aissistant.demo.models.ERole;
import com.aissistant.demo.models.Role;
import com.aissistant.demo.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            System.out.println("Seeding roles...");
            roleRepository.save(new Role(ERole.ROLE_USER));
            roleRepository.save(new Role(ERole.ROLE_ASSISTANT));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        } else {
            System.out.println("Roles already exist. Skipping seeding.");
        }
    }
}
