package com.modura.modura_server.global.response.code.status;

import com.modura.modura_server.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    // Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "찾을 수 없는 요청입니다."),

    // 멤버 관려 에러
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "닉네임은 필수입니다."),
    TERMS_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4003", "이용약관이 없습니다."),
    ADDRESS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER4004", "거주지가 이미 존재합니다."),

    // 카테고리 관련 에러
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY4001", "카테고리를 찾을 수 없습니다."),
  
   // 컨텐츠 관련 에러
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT4001", "컨텐츠가 없습니다."),
    CONTENT_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT4002", "컨텐츠 리뷰가 없습니다."),

    // 장소 관련 에러
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE4001", "장소가 없습니다."),
    PLACE_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE4002", "장소 리뷰가 없습니다."),

    // 스틸컷 관련 에러
    STILLCUT_NOT_FOUND(HttpStatus.NOT_FOUND, "STILLCUT4001", "스틸컷이 없습니다."),
    STILLCUT_PLACE_MISMATCH(HttpStatus.BAD_REQUEST, "STILLCUT4002", "스틸컷이 해당 장소에 속하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}