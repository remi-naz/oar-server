package dev.ridill.oar_server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserKeyWrapRepository extends JpaRepository<UserKeyWrap, UUID> {

    Optional<UserKeyWrap> findByUserIdAndKeyVersionAndWrapMethod(UUID userId, int keyVersion, WrapMethod wrapMethod);

    List<UserKeyWrap> findByUserId(UUID userId);

    /**
     * Hard delete. Retained key material for a dead account is pure liability.
     */
    long deleteByUserId(UUID userId);
}
