package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.request.LoginDTO;
import com.portfolio.stocksage.dto.request.SignupDTO;
import com.portfolio.stocksage.dto.response.JwtDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import com.portfolio.stocksage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<JwtDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtDTO jwtDTO = authService.login(loginDTO);
        return ResponseEntity.ok(jwtDTO);
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Registers a new user in the system")
    public ResponseEntity<UserDTO> signup(@Valid @RequestBody SignupDTO signupDTO) {
        UserDTO userDTO = authService.signup(signupDTO);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
}