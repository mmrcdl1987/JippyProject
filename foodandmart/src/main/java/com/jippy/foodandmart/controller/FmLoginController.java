package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmPermission;
import com.jippy.foodandmart.entity.FmRolePermissions;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.entity.FmUserRolePermissions;
import com.jippy.foodandmart.security.JwtUtils;
import com.jippy.foodandmart.service.OtpLoginService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/fm/auth")
public class FmLoginController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpLoginService otpLoginService;


    @PostMapping(path = "/login")
    @SecurityRequirements
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            // 1. Validate username/password against DB (via UserDetailsService + BCrypt)
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. If successful, generate JWT
            String jwt = jwtUtils.generateToken(authentication);

            // 2. Fetch the authenticated principal
            FmUser user = (FmUser) authentication.getPrincipal();

            // 4. Extract roles as a list of strings
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            AuthResponseDto authResponseDto = new AuthResponseDto();

            authResponseDto.setJwt(jwt);
            authResponseDto.setUserType(user.getUserType());
            authResponseDto.setUserId(user.getUserId());
            authResponseDto.setRoles(roles);

            return ResponseEntity.ok(authResponseDto);

        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID CREDENTIALS [ Enter Valid Credentials] " +
                    "OR You Are Not APPROVED [ Check your APPROVAL STATUS]");
        }
    }

    @PostMapping(path = "/webLogin")
    @SecurityRequirements
    public ResponseEntity<?> webLogin(@RequestBody LoginRequestDto loginRequest) {
        try {
            // 1. Validate username/password against DB (via UserDetailsService + BCrypt)
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. If successful, generate JWT
            String jwt = jwtUtils.generateToken(authentication);

            // 2. Fetch the authenticated principal
            FmUser user = (FmUser) authentication.getPrincipal();

            // 4. Extract roles as a list of strings
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<String> permissions =
                    user.getUserRolePermissions()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(FmUserRolePermissions::getRolePermission)
                            .filter(Objects::nonNull)
                            .map(FmRolePermissions::getRole)
                            .filter(Objects::nonNull)
                            .flatMap(roleObj -> roleObj.getPermissions().stream())
                            .map(FmPermission::getPermissionName)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

            log.info(
                    "Login successful | username={} | role={}",
                    user.getUsername(),roles
            );

            WebAuthResponseDto authResponseDto = new WebAuthResponseDto();

            authResponseDto.setToken(jwt);
            authResponseDto.setUserType(user.getUserType());
            authResponseDto.setUserId(user.getUserId());
            authResponseDto.setRole(roles);
            authResponseDto.setPermissions(permissions);
            authResponseDto.setUsername(user.getUsername());

            return ResponseEntity.ok(authResponseDto);

        } catch (AuthenticationException e) {
            log.warn(
                    "Login failed for username={}",
                    loginRequest.getUsername());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }


    @PostMapping("/send-login-otp")
    @SecurityRequirements
    public ResponseEntity<SendOtpResponseDto> sendLoginOtp(
            @Valid @RequestBody SendLoginOtpRequestDto requestDto
    ) {

        SendOtpResponseDto response =
                otpLoginService.sendLoginOtp(requestDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-login-otp")
    @SecurityRequirements
    public ResponseEntity<SendOtpResponseDto> resendLoginOtp(
            @Valid @RequestBody SendLoginOtpRequestDto requestDto
    ) {

        SendOtpResponseDto response =
                otpLoginService.resendLoginOtp(requestDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-login-otp")
    @SecurityRequirements
    public ResponseEntity<AuthResponseDto> verifyLoginOtp(
            @Valid @RequestBody VerifyLoginOtpRequestDto requestDto
    ) {

        AuthResponseDto response =
                otpLoginService.verifyLoginOtp(requestDto);

        return ResponseEntity.ok(response);
    }
}
