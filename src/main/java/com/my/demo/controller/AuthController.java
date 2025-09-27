package com.my.demo.controller;

import com.my.demo.annotation.ThrowsMessage;
import com.my.demo.dto.LoginDTO;
import com.my.demo.dto.RefreshTokenRequestDTO;
import com.my.demo.dto.UserDTO;
import com.my.demo.model.RefreshToken;
import com.my.demo.model.User;
import com.my.demo.response.ApiMessage;
import com.my.demo.response.ApiResponse;
import com.my.demo.service.RefreshTokenService;
import com.my.demo.service.UserService;
import com.my.demo.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(UserService userService, TokenService tokenService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginDTO loginDTO) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // create a token
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var userOptional = userService.getUserByEmail(loginDTO.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                String accessToken = tokenService.generateAccessToken(user.getUsername());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
                UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("accessToken", accessToken);
                responseData.put("refreshToken", refreshToken.getToken());
                responseData.put("user", userDTO);
                responseData.put("tokenType", "Bearer");

                ApiResponse<Map<String, Object>> response = ApiResponse.success(responseData, "Login successful");
                return ResponseEntity.ok(response);
            }
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.error(401, "Invalid username or password", "AUTH_FAILED");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = tokenService.generateAccessToken(user.getUsername());

                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("accessToken", newAccessToken);
                    responseData.put("refreshToken", requestRefreshToken);
                    responseData.put("tokenType", "Bearer");

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(responseData, "Token refreshed successfully");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<Map<String, Object>> response = ApiResponse.error(403, "Invalid refresh token", "INVALID_REFRESH_TOKEN");
                    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        refreshTokenService.deleteByToken(refreshToken);

        ApiResponse<Void> response = ApiResponse.success(null, "Logout successful");
        return ResponseEntity.ok(response);
    }
}
