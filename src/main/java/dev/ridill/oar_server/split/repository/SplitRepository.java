package dev.ridill.oar_server.split.repository;

import dev.ridill.oar_server.split.entity.Split;
import dev.ridill.oar_server.split.entity.SplitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SplitRepository extends JpaRepository<Split, UUID> {

    List<Split> findByOwnerIdAndStatus(UUID ownerId, SplitStatus status);

    List<Split> findByStatus(SplitStatus status);

    @Query("""
            SELECT s FROM Split s
            WHERE s.status = :status
            AND s.id IN (SELECT sh.split.id FROM SplitShare sh WHERE sh.user.id = :participantId)
            """)
    List<Split> findByParticipantIdAndStatus(@Param("participantId") UUID participantId,
                                              @Param("status") SplitStatus status);
}
