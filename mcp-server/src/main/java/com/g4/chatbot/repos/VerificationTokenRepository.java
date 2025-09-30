package com.g4.chatbot.repos;

import com.g4.chatbot.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserIdAndUserType(Long userId, String userType);
    Optional<VerificationToken> findFirstByUserTypeOrderByCreatedDateDesc(String userType);
}