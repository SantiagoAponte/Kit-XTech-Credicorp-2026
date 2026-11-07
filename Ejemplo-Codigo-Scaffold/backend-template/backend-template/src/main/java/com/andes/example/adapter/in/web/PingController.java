package com.andes.example.adapter.in.web;

import com.andes.example.application.dto.CreatePingCommand;
import com.andes.example.application.dto.CreatePingResult;
import com.andes.example.application.port.in.CreatePing;
import com.andes.example.application.port.in.GetPing;
import com.andes.example.application.port.in.ListPings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Sin lógica de negocio: valida formato, traduce y delega en puertos de entrada. */
@RestController
@RequestMapping("/api/v1/pings")
public class PingController {

    private final CreatePing createPing;
    private final GetPing getPing;
    private final ListPings listPings;

    public PingController(CreatePing createPing, GetPing getPing, ListPings listPings) {
        this.createPing = createPing;
        this.getPing = getPing;
        this.listPings = listPings;
    }

    @PostMapping
    public ResponseEntity<PingResponse> create(
            @RequestHeader("Idempotency-Key") @Size(min = 1, max = 64) String idempotencyKey,
            @Valid @RequestBody CreatePingRequest request) {
        CreatePingResult result = createPing.execute(new CreatePingCommand(idempotencyKey, request.message(),
                new BigDecimal(request.amount().amount()), request.amount().currency()));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(PingResponse.from(result.ping()));
    }

    @GetMapping
    public PingPageResponse list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PingPageResponse.from(listPings.execute(page, size));
    }

    @GetMapping("/{id}")
    public PingResponse get(@PathVariable String id) {
        return PingResponse.from(getPing.execute(id));
    }
}
