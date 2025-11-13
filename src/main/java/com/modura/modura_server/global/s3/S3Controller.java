package com.modura.modura_server.global.s3;

import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
@Tag(name = "Image")
public class S3Controller {

    private final S3Service s3Service;

    /**
     *  업로드용 Presigned URL 발급
     * 프론트에서 파일 업로드 전에 요청
     */
    @Operation(summary = "업로드용 Presigned URL 발급")
    @PostMapping("/presigned-upload")
    public ApiResponse<List<S3ResponseDTO.PresignedUrlResDTO>> getPresignedUploadUrl(
            @Valid @RequestBody S3RequestDTO.PresignedUploadReqDTO req
    ) {
        List<S3ResponseDTO.PresignedUrlResDTO> response = s3Service.generateUploadPresignedUrl(req);
        return ApiResponse.onSuccess(response);
    }
}
