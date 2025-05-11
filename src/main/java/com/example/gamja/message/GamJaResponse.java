package com.example.gamja.message;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamJaResponse {
    private String code;
    private String message;

    private Object data; // 데이터 담는 필드 추가!

    public static GamJaResponse success(String message, Object data) {
        return new GamJaResponse("SUCCESS", message, data);
    }

    public static GamJaResponse ok(String message) {
        return new GamJaResponse("OK", message, null);
    }
    public static GamJaResponse fail(String message) {
        return new GamJaResponse("FAIL", message, null);
    }

    public static GamJaResponse error(String message) {
        return new GamJaResponse("ERROR", message, null);
    }
}
