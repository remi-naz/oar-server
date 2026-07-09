package dev.ridill.oar_server.split.controller;

import dev.ridill.oar_server.split.dto.CreateSplitRequest;
import dev.ridill.oar_server.split.dto.SplitResponse;
import dev.ridill.oar_server.split.service.SplitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/splits")
public class SplitController {

    private final SplitService splitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SplitResponse createSplit(@Valid @RequestBody CreateSplitRequest request) {
        return splitService.createSplit(request);
    }

    @GetMapping("/{id}")
    public SplitResponse getSplit(@PathVariable UUID id) {
        return splitService.getSplit(id);
    }

    @GetMapping
    public List<SplitResponse> listSplits(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) UUID participantId
    ) {
        return splitService.listSplits(ownerId, participantId);
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<Void> voidSplit(@PathVariable UUID id) {
        splitService.voidSplit(id);
        return ResponseEntity.noContent().build();
    }
}
