package ru.rest.wallet.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rest.wallet.dto.TransferRequest;
import ru.rest.wallet.dto.TransferResponse;
import ru.rest.wallet.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(
            TransferService transferService
    ) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponse transfer(
            @Valid @RequestBody TransferRequest request
    ) {

        return transferService.transfer(request);
    }
}