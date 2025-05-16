package org.example.prodcatservice.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.prodcatservice.dtos.common.BaseResponse;
import org.example.prodcatservice.dtos.product.responseDtos.InventoryLogResponseDto;
import org.example.prodcatservice.dtos.product.responseDtos.TokenIntrospectionResponseDTO;
import org.example.prodcatservice.models.InventoryAuditLog;
import org.example.prodcatservice.repositories.InventoryAuditLogRepository;
import org.example.prodcatservice.services.TokenService;
import org.example.prodcatservice.utils.TokenClaimUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.example.prodcatservice.utils.JwtClaimUtils.hasRole;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/inventory-logs")
@Tag(name = "Admin - Inventory Logs")
public class InventoryAuditLogController {

    private final InventoryAuditLogRepository inventoryAuditLogRepository;
    private final Counter inventoryLogAccessCounter;
    private final TokenService tokenService;

    public InventoryAuditLogController(InventoryAuditLogRepository inventoryAuditLogRepository,
                                       MeterRegistry registry,
                                       TokenService tokenService) {
        this.inventoryAuditLogRepository = inventoryAuditLogRepository;
        this.inventoryLogAccessCounter = registry.counter("inventory.logs.accessed");
        this.tokenService = tokenService;
    }

    @Operation(
            summary = "Paginated inventory log viewer (admin-only)",
            description = "Lists inventory change logs for auditing. Admin access required.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logs retrieved",
                            content = @Content(mediaType = "application/json", examples = {
                                    @ExampleObject(name = "Success", value = "{ \"status\": \"SUCCESS\", \"message\": \"Logs fetched\", \"data\": [ { \"productId\": 101, \"previousQuantity\": 10, \"newQuantity\": 8, \"updatedBy\": \"order-service\" } ] }")
                            })
                    ),
                    @ApiResponse(responseCode = "403", description = "Access denied for non-admins")
            }
    )
    @GetMapping
    public ResponseEntity<BaseResponse<Page<InventoryLogResponseDto>>> getInventoryLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String updatedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestHeader("Authorization") String tokenHeader) {

        inventoryLogAccessCounter.increment();

//        if (!hasRole(jwt, "ADMIN")) {
//            return ResponseEntity.status(403).body(BaseResponse.failure("Access Denied"));
//        }

        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            return ResponseEntity.status(403).body(BaseResponse.failure("Access Denied"));
        }

        Page<InventoryAuditLog> logs = inventoryAuditLogRepository.searchLogs(
                productId, updatedBy, startDate, endDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))
        );

        Page<InventoryLogResponseDto> response = logs.map(log -> new InventoryLogResponseDto(
                log.getId(),
                log.getProductId(),
                log.getPreviousQuantity(),
                log.getNewQuantity(),
                log.getUpdatedBy(),
                log.getReason(),
                log.getTimestamp()
        ));

        return ResponseEntity.ok(BaseResponse.success("Logs fetched", response));
    }

}
