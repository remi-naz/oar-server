package dev.ridill.oar_server.split.repository;

import dev.ridill.oar_server.split.entity.SplitShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SplitShareRepository extends JpaRepository<SplitShare, UUID> {

    List<SplitShare> findBySplitId(UUID splitId);

    List<SplitShare> findBySplitIdIn(Collection<UUID> splitIds);

    boolean existsBySplitIdAndUserId(UUID splitId, UUID userId);
}
