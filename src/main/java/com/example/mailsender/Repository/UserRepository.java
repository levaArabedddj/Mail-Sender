package com.example.mailsender.Repository;

import com.example.mailsender.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByConfirmationToken(String confirmationToken);

    Users findByEmail(String email);



}
