package com.dormitory.service;

import com.dormitory.dto.request.GoogleAuthRequest;
import com.dormitory.dto.request.LoginRequest;
import com.dormitory.dto.request.OtpVerifyRequest;
import com.dormitory.dto.request.RegisterRequest;
import com.dormitory.dto.request.ResendOtpRequest;
import com.dormitory.dto.response.AuthResponse;
import com.dormitory.dto.response.OtpVerifyResponse;
import com.dormitory.entity.RefreshToken;
import com.dormitory.entity.User;
import com.dormitory.repository.RefreshTokenRepository;
import com.dormitory.repository.UserRepository;
import com.dormitory.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id:default_client_id}")
    private String googleClientId;

    // ── In-memory OTP store: email → OtpEntry ──────────────────────────────────
    // In production, replace with Redis-backed cache.
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    private record OtpEntry(String code, Instant expiresAt) {}

    // ── Register ───────────────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        // 1. Check duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered.");
        }

        // 2. Check duplicate phone (if provided)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            boolean phoneExists = userRepository.findAll().stream()
                    .anyMatch(u -> request.getPhone().equals(u.getPhone()));
            if (phoneExists) {
                throw new RuntimeException("Phone number is already in use.");
            }
        }

        // 3. LANDLORD-specific validation
        if (request.getRole() == User.Role.LANDLORD) {
            if (request.getIdentityNumber() == null || request.getIdentityNumber().isBlank()) {
                throw new RuntimeException("Identity number (CCCD) is required for Landlord registration.");
            }
        }

        // 4. Create user with PENDING status
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStatus(User.Status.PENDING);
        user.setLandlordVerified(false);

        // 5. Save LANDLORD extra info
        if (request.getRole() == User.Role.LANDLORD) {
            user.setIdentityNumber(request.getIdentityNumber());
            user.setTaxCode(request.getTaxCode());
            user.setBusinessLicense(request.getBusinessLicense());
        }

        userRepository.save(user);

        // 6. Generate and store OTP
        String otp = generateOtp();
        otpStore.put(request.getEmail(), new OtpEntry(otp, Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)));

        // 7. Log OTP to console (dev mode)
        System.out.println("========================================");
        System.out.println("[DEV] OTP for " + request.getEmail() + " : " + otp);
        if (request.getRole() == User.Role.LANDLORD) {
            System.out.println("[DEV] Landlord account — pending admin verification after OTP.");
        }
        System.out.println("========================================");
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    @Transactional
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        OtpEntry entry = otpStore.get(request.getEmail());

        if (entry == null) {
            throw new RuntimeException("No OTP found for this email. Please register or request a new code.");
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(request.getEmail());
            throw new RuntimeException("OTP has expired. Please request a new code.");
        }

        if (!entry.code().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP code. Please try again.");
        }

        // Activate the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Account not found."));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // Remove used OTP
        otpStore.remove(request.getEmail());

        // Return role & landlordVerified so frontend can redirect correctly
        String message = user.getRole() == User.Role.LANDLORD
                ? "Email verified. Your landlord account is pending admin approval."
                : "Email verified. You can now sign in.";

        return new OtpVerifyResponse(
                user.getRole().name(),
                Boolean.TRUE.equals(user.getLandlordVerified()),
                message
        );
    }

    // ── Resend OTP ────────────────────────────────────────────────────────────

    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email."));

        if (user.getStatus() == User.Status.ACTIVE) {
            throw new RuntimeException("This account is already verified.");
        }

        String otp = generateOtp();
        otpStore.put(request.getEmail(), new OtpEntry(otp, Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)));

        System.out.println("========================================");
        System.out.println("[DEV] Resend OTP for " + request.getEmail() + " : " + otp);
        System.out.println("========================================");
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new RuntimeException("Account not verified. Please check your email for the OTP code.");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("Your account has been suspended. Please contact support.");
        }

        return generateAuthResponse(user);
    }

    // ── Google OAuth ──────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        try {
            System.out.println("Authenticating with Google Client ID: " + googleClientId);
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String subject = payload.getSubject();

                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null) {
                    // New user — use the role selected on the Register page
                    User.Role selectedRole = request.getRole() != null ? request.getRole() : User.Role.TENANT;
                    user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setAvatarUrl(pictureUrl);
                    user.setGoogleId(subject);
                    user.setRole(selectedRole);
                    user.setStatus(User.Status.ACTIVE);
                    user.setLandlordVerified(false); // always false on creation, admin must verify
                    // Generate a random password for google-only users
                    user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user = userRepository.save(user);

                    if (selectedRole == User.Role.LANDLORD) {
                        System.out.println("========================================");
                        System.out.println("[DEV] New LANDLORD registered via Google: " + email);
                        System.out.println("[DEV] landlordVerified=false — pending admin approval.");
                        System.out.println("========================================");
                    }
                } else if (user.getGoogleId() == null) {
                    user.setGoogleId(subject);
                    userRepository.save(user);
                }

                return generateAuthResponse(user);
            } else {
                throw new RuntimeException("Invalid ID token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshAccessToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        String accessToken = tokenProvider.createAccessToken(user.getId().toString(), user.getRole().name());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getExpirationDate().getTime(),
                user.getRole().name()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse generateAuthResponse(User user) {
        boolean landlordVerified = Boolean.TRUE.equals(user.getLandlordVerified());
        boolean identityInfoSubmitted = user.getIdentityNumber() != null && !user.getIdentityNumber().isBlank();

        String accessToken = tokenProvider.createAccessToken(
                user.getId().toString(),
                user.getRole().name(),
                landlordVerified
        );
        String refreshTokenString = tokenProvider.createRefreshToken(user.getId().toString());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenString);
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                tokenProvider.getExpirationDate().getTime(),
                user.getRole().name(),
                landlordVerified,
                identityInfoSubmitted
        );
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }
}
