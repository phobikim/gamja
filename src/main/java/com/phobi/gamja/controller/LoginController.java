package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.user.UserRepository;
import com.phobi.gamja.service.AuthService;
import com.phobi.gamja.web.config.annotation.SanitizeInput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<GamJaResponse> getMyInfo(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getMyInfo(request));
    }

    @PostMapping("/login")
    @SanitizeInput
    public ResponseEntity<GamJaResponse> login(@RequestParam String username,
                                               @RequestParam String pin,
                                               HttpServletRequest request,
                                               HttpSession session) {
        return ResponseEntity.ok(authService.login(username, pin, request, session));
    }

    @PostMapping("/signup")
    @SanitizeInput
    public ResponseEntity<GamJaResponse> signup(@RequestParam String username,
                                                @RequestParam String pin,
                                                HttpServletRequest request,
                                                HttpSession session) {
        return ResponseEntity.ok(authService.signup(username, pin, request, session));
    }

    @GetMapping("/check-username")
    @SanitizeInput
    public ResponseEntity<GamJaResponse> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(authService.checkUsername(username));
    }

    @GetMapping("/session-check")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId != null && userRepository.existsById(userId)) {
            return ResponseEntity.ok(Map.of("code", "SUCCESS"));
        } else {
            request.getSession().invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "NO_VALID_USER"));
        }
    }

}
