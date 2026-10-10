package com.staysphere.auth.repository;

import com.staysphere.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, " +
            "r.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE r.userId = :userId AND r.revoked = false")
    void revokeAllUserTokens(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, " +
            "r.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE r.token = :token")
    void revokeToken(@Param("token") String token);
}