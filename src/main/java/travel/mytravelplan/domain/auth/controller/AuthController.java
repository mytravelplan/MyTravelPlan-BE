package travel.mytravelplan.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.mytravelplan.domain.auth.dto.AdditionalInfoRequestDto;
import travel.mytravelplan.domain.auth.dto.JwtTokenDto;
import travel.mytravelplan.domain.auth.service.AuthService;
import travel.mytravelplan.global.common.response.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 로그아웃
    @PostMapping("/sign-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    // 추가적인 정보 입력
    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<Void>> addAdditionalInfo(HttpServletRequest request, HttpServletResponse response, @RequestBody @Validated AdditionalInfoRequestDto additionalInfoRequestDto) {
        authService.addAdditionalInfo(request, response, additionalInfoRequestDto);
        return ResponseEntity.noContent().build();
    }
}
