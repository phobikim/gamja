package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<GamJaResponse> getMyInfo(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getMyInfo(request));
    }

    @PostMapping("/login")
    public ResponseEntity<GamJaResponse> login(@RequestParam String username,
                                               @RequestParam String pin,
                                               HttpServletRequest request,
                                               HttpSession session) {
        return ResponseEntity.ok(authService.login(username, pin, request, session));
    }

    @PostMapping("/signup")
    public ResponseEntity<GamJaResponse> signup(@RequestParam String username,
                                                @RequestParam String pin,
                                                HttpServletRequest request,
                                                HttpSession session) {
        return ResponseEntity.ok(authService.signup(username, pin, request, session));
    }

    @GetMapping("/check-username")
    public ResponseEntity<GamJaResponse> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(authService.checkUsername(username));
    }

}
