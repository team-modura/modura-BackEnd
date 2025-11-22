package com.modura.modura_server.domain.admin.controller;

import com.modura.modura_server.domain.admin.dto.AdminRequestDTO;
import com.modura.modura_server.domain.admin.service.AdminCommandService;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin")
@Validated
public class AdminController {

    private final AdminCommandService adminCommandService;

    @Operation(summary = "촬영지 추가")
    @PostMapping("/places")
    public ApiResponse<Void> createPlace(@Valid @RequestBody AdminRequestDTO.CreatePlaceDTO request) {

        adminCommandService.createPlace(request);
        return ApiResponse.of(SuccessStatus._CREATED, null);
    }

    @Operation(summary = "스틸컷 추가")
    @PostMapping("/stillcuts")
    public ApiResponse<Void> createStillcut(@Valid @RequestBody AdminRequestDTO.CreateStillcutDTO request) {

        adminCommandService.createStillcut(request);
        return ApiResponse.of(SuccessStatus._CREATED, null);
    }
}