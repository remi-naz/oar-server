package dev.ridill.oar_server.split.repository;

import dev.ridill.oar_server.split.entity.SplitPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SplitPaymentRepository extends JpaRepository<SplitPayment, UUID> {

    List<SplitPayment> findBySplitId(UUID splitId);

    List<SplitPayment> findBySplitIdIn(Collection<UUID> splitIds);
}
