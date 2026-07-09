package dev.ridill.oar_server.split.service;

import dev.ridill.oar_server.common.error.BusinessRuleException;
import dev.ridill.oar_server.common.error.ResourceNotFoundException;
import dev.ridill.oar_server.split.dto.CreateSplitRequest;
import dev.ridill.oar_server.split.dto.SplitParticipantAmount;
import dev.ridill.oar_server.split.dto.SplitResponse;
import dev.ridill.oar_server.split.entity.Split;
import dev.ridill.oar_server.split.entity.SplitPayment;
import dev.ridill.oar_server.split.entity.SplitShare;
import dev.ridill.oar_server.split.entity.SplitStatus;
import dev.ridill.oar_server.split.repository.SplitPaymentRepository;
import dev.ridill.oar_server.split.repository.SplitRepository;
import dev.ridill.oar_server.split.repository.SplitShareRepository;
import dev.ridill.oar_server.user.User;
import dev.ridill.oar_server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SplitService {

    private final SplitRepository splitRepository;
    private final SplitPaymentRepository splitPaymentRepository;
    private final SplitShareRepository splitShareRepository;
    private final UserRepository userRepository;

    @Transactional
    public SplitResponse createSplit(CreateSplitRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.ownerId()));

        Set<UUID> shareUserIds = request.shares().stream()
                .map(SplitParticipantAmount::userId)
                .collect(Collectors.toSet());
        if (shareUserIds.size() != request.shares().size()) {
            throw new BusinessRuleException("Duplicate userId in shares");
        }

        Set<UUID> participantIds = Stream.concat(
                        request.payments().stream().map(SplitParticipantAmount::userId),
                        request.shares().stream().map(SplitParticipantAmount::userId))
                .collect(Collectors.toSet());
        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new BusinessRuleException("One or more participant userIds do not exist");
        }
        Map<UUID, User> usersById = participants.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        BigDecimal shareSum = request.shares().stream()
                .map(SplitParticipantAmount::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (shareSum.compareTo(request.totalAmount()) != 0) {
            throw new BusinessRuleException("Sum of shares must equal totalAmount");
        }

        Split split = new Split();
        split.setNote(request.note());
        split.setTotalAmount(request.totalAmount());
        split.setOwner(owner);
        split.setStatus(SplitStatus.ACTIVE);
        split = splitRepository.save(split);

        List<SplitPayment> payments = new ArrayList<>();
        for (SplitParticipantAmount p : request.payments()) {
            SplitPayment payment = new SplitPayment();
            payment.setSplit(split);
            payment.setUser(usersById.get(p.userId()));
            payment.setAmount(p.amount());
            payments.add(payment);
        }
        payments = splitPaymentRepository.saveAll(payments);

        List<SplitShare> shares = new ArrayList<>();
        for (SplitParticipantAmount s : request.shares()) {
            SplitShare share = new SplitShare();
            share.setSplit(split);
            share.setUser(usersById.get(s.userId()));
            share.setAmountOwed(s.amount());
            shares.add(share);
        }
        shares = splitShareRepository.saveAll(shares);

        return toResponse(split, payments, shares);
    }

    @Transactional(readOnly = true)
    public SplitResponse getSplit(UUID id) {
        Split split = splitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found: " + id));
        List<SplitPayment> payments = splitPaymentRepository.findBySplitId(id);
        List<SplitShare> shares = splitShareRepository.findBySplitId(id);
        return toResponse(split, payments, shares);
    }

    @Transactional(readOnly = true)
    public List<SplitResponse> listSplits(UUID ownerId, UUID participantId) {
        List<Split> splits;
        if (ownerId != null) {
            splits = splitRepository.findByOwnerIdAndStatus(ownerId, SplitStatus.ACTIVE);
            if (participantId != null) {
                splits = splits.stream()
                        .filter(s -> splitShareRepository.existsBySplitIdAndUserId(s.getId(), participantId))
                        .toList();
            }
        } else if (participantId != null) {
            splits = splitRepository.findByParticipantIdAndStatus(participantId, SplitStatus.ACTIVE);
        } else {
            splits = splitRepository.findByStatus(SplitStatus.ACTIVE);
        }

        List<UUID> splitIds = splits.stream().map(Split::getId).toList();
        Map<UUID, List<SplitPayment>> paymentsBySplit = splitPaymentRepository.findBySplitIdIn(splitIds).stream()
                .collect(Collectors.groupingBy(p -> p.getSplit().getId()));
        Map<UUID, List<SplitShare>> sharesBySplit = splitShareRepository.findBySplitIdIn(splitIds).stream()
                .collect(Collectors.groupingBy(s -> s.getSplit().getId()));

        return splits.stream()
                .map(split -> toResponse(
                        split,
                        paymentsBySplit.getOrDefault(split.getId(), List.of()),
                        sharesBySplit.getOrDefault(split.getId(), List.of())))
                .toList();
    }

    @Transactional
    public void voidSplit(UUID id) {
        Split split = splitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split not found: " + id));
        if (split.getStatus() == SplitStatus.VOIDED) {
            throw new BusinessRuleException("Split is already voided");
        }
        split.setStatus(SplitStatus.VOIDED);
        splitRepository.save(split);
    }

    private SplitResponse toResponse(Split split, List<SplitPayment> payments, List<SplitShare> shares) {
        List<SplitResponse.Line> paymentLines = payments.stream()
                .map(p -> new SplitResponse.Line(p.getUser().getId(), p.getAmount()))
                .toList();
        List<SplitResponse.Line> shareLines = shares.stream()
                .map(s -> new SplitResponse.Line(s.getUser().getId(), s.getAmountOwed()))
                .toList();
        return new SplitResponse(
                split.getId(),
                split.getNote(),
                split.getTotalAmount(),
                split.getOwner().getId(),
                split.getStatus(),
                split.getCreatedAt(),
                split.getUpdatedAt(),
                paymentLines,
                shareLines
        );
    }
}
