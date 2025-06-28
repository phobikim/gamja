package com.phobi.gamja.web.config;

import com.phobi.gamja.message.GamJaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 파라미터, 조건 등에서 발생하는 예외
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GamJaResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(GamJaResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GamJaResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(GamJaResponse.fail(e.getMessage()));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GamJaResponse> handleException(Exception e) {
        e.printStackTrace(); // 로그 확인용
        return ResponseEntity.internalServerError()
                .body(GamJaResponse.fail("서버 오류가 발생했습니다."));
    }
}