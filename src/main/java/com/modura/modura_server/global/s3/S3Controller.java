package com.modura.modura_server.global.s3;

import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            @RequestBody S3RequestDTO.PresignedUploadReqDTO req
    ) {
        List<S3ResponseDTO.PresignedUrlResDTO> result = new ArrayList<>();
        for (int i = 0; i < req.getFileNames().size(); i++) {
            String key = req.getFolder() + "/" + UUID.randomUUID() + "-" + req.getFileNames().get(i);
            String url = s3Service.generateUploadPresignedUrlWithKey(key, req.getContentTypes().get(i));
            result.add(new S3ResponseDTO.PresignedUrlResDTO(key, url));
        }
        return ApiResponse.onSuccess(result);
    }


    /**
     * 조회용 Presigned URL 발급
     * 프론트에서 비공개 이미지 조회 시 요청
     */
    @Operation(summary = "조회용 Presigned URL 발급")
    @GetMapping("/presigned-view")
    public ApiResponse<String> getPresignedViewUrl(
            @RequestParam String key // 예: ingredient/uuid-filename.jpg
    ) {
        String viewUrl = s3Service.generateViewPresignedUrl(key);
        return ApiResponse.onSuccess(viewUrl);
    }


    //이미지 삭제 (서버가 직접 삭제 요청)
    @Operation(summary = "이미지 삭제")
    @DeleteMapping("/file")
    public ApiResponse<Void> deleteImage(@RequestParam String imageUrl) {
        s3Service.deleteFile(imageUrl);
        return ApiResponse.onSuccess(null);
    }
}
