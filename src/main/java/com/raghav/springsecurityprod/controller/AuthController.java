package com.raghav.springsecurityprod.controller;

import com.raghav.springsecurityprod.dto.LoginRequest;
import com.raghav.springsecurityprod.dto.RegisterRequest;
import com.raghav.springsecurityprod.entity.RefreshToken;
import com.raghav.springsecurityprod.entity.Role;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.repo.UserRepository;
import com.raghav.springsecurityprod.service.JwtService;
import com.raghav.springsecurityprod.service.RefreshTokenService;
import com.raghav.springsecurityprod.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {

        User user = userService.register(request);
        return issueTokensAndRespond(user, response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,HttpServletResponse response){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issueRefreshToken(user);

        setRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, user.getEmail(), user.getRoles()));
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token",required = false)String refreshToken,
                                                HttpServletResponse response){
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RefreshTokenService.RotationResult result =     refreshTokenService.rotate(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(result.user());
        setRefreshCookie(response, result.newRawToken());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, result.user().getEmail(), result.user().getRoles()));

    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }


    //-----Helpers----------
    private ResponseEntity<AuthResponse> issueTokensAndRespond(
            User user,HttpServletResponse response,HttpStatus status){
        String accessToken = jwtService.generateAccessToken(user);
        String refreshtoken = refreshTokenService.issueRefreshToken(user);
        setRefreshCookie(response,refreshtoken);
        return ResponseEntity.status(status).body(new AuthResponse(accessToken,user.getEmail(),user.getRoles()));
    }
    private void setRefreshCookie(HttpServletResponse response,String token){
        ResponseCookie cookie = ResponseCookie.from("refresh_token",token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(refreshExpiryMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }
    private void clearRefreshCookie(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("refresh_token","")
                .httpOnly(true).secure(true).sameSite("Strict").path("/api/auth").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }
}
    record AuthResponse(String accessToken, String email, Set<Role> roles) {};
