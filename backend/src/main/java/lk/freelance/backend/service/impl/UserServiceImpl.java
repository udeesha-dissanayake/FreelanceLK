package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.VerificationStatus;
import lk.freelance.backend.exception.*;
import lk.freelance.backend.repository.*;
import lk.freelance.backend.service.TrustScoreService;
import lk.freelance.backend.service.UserService;
import lk.freelance.backend.service.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrustScoreService trustScoreService;
    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    private final VerificationMapper verificationMapper;
    private final WithdrawalMapper withdrawalMapper;

    // ... (Profile and Password methods remain the same) ...
    @Override
    public UserProfileDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        TrustScore trustScore = trustScoreRepository.findByUser_UserId(userId).orElse(null);
        List<UserSkill> skills = userSkillRepository.findByUser_UserId(userId);
        return userMapper.toUserProfileDTO(user, profile, trustScore, skills);
    }

    @Override
    @Transactional
    public UserProfileDTO updateUserProfile(UUID userId, UserUpdateRequest request) {
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getDisplayName() != null) profile.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getTimezone() != null) profile.setTimezone(request.getTimezone());

        profile.setUpdatedAt(LocalDateTime.now());
        profile = userProfileRepository.save(profile);

        return userMapper.toUserProfileDTO(profile.getUser(), profile,
                trustScoreRepository.findByUser_UserId(userId).orElse(null),
                userSkillRepository.findByUser_UserId(userId));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // --- 1. Fix Ambiguous toDTO Reference ---
    @Override
    public List<SkillDTO> getUserSkills(UUID userId) {
        return userSkillRepository.findByUser_UserId(userId).stream()
                // FIX: Use explicit lambda instead of method reference (skillMapper::toDTO)
                // because compiler is confused between toDTO(Skill) and toDTO(UserSkill)
                .map(userSkill -> skillMapper.toDTO(userSkill))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SkillDTO addSkillToUser(UUID userId, AddSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Skill skill = skillRepository.findBySkillName(request.getSkillName())
                .orElseGet(() -> skillRepository.save(Skill.builder()
                        .skillName(request.getSkillName())
                        .category(request.getCategory())
                        .createdAt(LocalDateTime.now())
                        .build()));

        if (userSkillRepository.existsByUser_UserIdAndSkill_SkillId(userId, skill.getSkillId())) {
            throw new BadRequestException("Skill already added to profile");
        }

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .proficiencyLevel(request.getProficiencyLevel())
                .yearsOfExperience(request.getYearsOfExperience())
                .createdAt(LocalDateTime.now())
                .build();

        return skillMapper.toDTO(userSkillRepository.save(userSkill));
    }

    @Override
    @Transactional
    public void removeSkillFromUser(UUID userId, UUID skillId) {
        UserSkill userSkill = userSkillRepository.findByUser_UserIdAndSkill_SkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
        userSkillRepository.delete(userSkill);
    }

    @Override
    @Transactional
    public VerificationDTO submitVerification(UUID userId, SubmitVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        IdentityVerification verification = IdentityVerification.builder()
                .user(user)
                .verificationType(request.getVerificationType())
                .documentNumber(request.getDocumentNumber())
                .documentUrl(request.getDocumentUrl())
                .verificationStatus(VerificationStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        verification = identityVerificationRepository.save(verification);
        trustScoreService.calculateTrustScore(userId);

        return verificationMapper.toDTO(verification);
    }

    @Override
    public VerificationDTO getVerificationStatus(UUID userId) {
        return identityVerificationRepository.findByUser_UserIdOrderByVerifiedAtDesc(userId).stream()
                .findFirst()
                .map(verificationMapper::toDTO)
                .orElse(null);
    }

    @Override
    public TrustScoreDTO getTrustScore(UUID userId) {
        return trustScoreService.getTrustScore(userId);
    }

    // --- 2. Dashboard Statistics ---
    @Override
    public UserStatisticsDTO getUserStatistics(UUID userId) {
        // 1. Completed orders
        long completedOrders = orderRepository.countBySeller_UserIdAndStatus(
                userId, lk.freelance.backend.enums.OrderStatus.COMPLETED);

        // 2. Active orders — only IN_PROGRESS (not all non-completed)
        long activeOrders = orderRepository.countBySeller_UserIdAndStatusIn(
                userId, java.util.List.of(
                        lk.freelance.backend.enums.OrderStatus.IN_PROGRESS,
                        lk.freelance.backend.enums.OrderStatus.ACCEPTED
                ));

        // 3. Total earned — sum of completed order amounts (real all-time earnings)
        Object rawTotal = orderRepository.sumAmountBySeller_UserIdAndStatus(
                userId, lk.freelance.backend.enums.OrderStatus.COMPLETED);
        java.math.BigDecimal totalEarned = (rawTotal instanceof java.math.BigDecimal bd)
                ? bd : java.math.BigDecimal.ZERO;

        // 4. Pending earnings — money held in escrow, not yet released
        java.math.BigDecimal pendingEarnings = walletRepository.findByUser_UserId(userId)
                .map(Wallet::getPendingBalance)
                .orElse(java.math.BigDecimal.ZERO);

        // 5. Trust score
        java.math.BigDecimal trustScore = trustScoreRepository.findByUser_UserId(userId)
                .map(ts -> ts.getOverallScore())
                .orElse(java.math.BigDecimal.ZERO);

        return UserStatisticsDTO.builder()
                .userId(userId)
                .activeOrders((int) activeOrders)
                .pendingEarnings(pendingEarnings)
                .totalEarned(totalEarned)
                .completedOrders((int) completedOrders)
                .trustScore(trustScore)
                .build();
    }

    @Override
    public WalletDTO getWallet(UUID userId) {
        Wallet wallet = walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", userId));
        return walletMapper.toDTO(wallet);
    }

    // --- 3. Fix Transaction Repository Error ---
    @Override
    public PagedResponse<TransactionDTO> getTransactionHistory(UUID userId, Integer page, Integer size) {
        Page<Transaction> transactionPage = transactionRepository.findByPayer_UserIdOrPayee_UserId(
                userId, userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<TransactionDTO> content = transactionPage.getContent().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page, transactionPage.getTotalPages(),
                transactionPage.getTotalElements(), size, transactionPage.hasNext(), transactionPage.hasPrevious());
    }

    // --- 4. Fix Ambiguous WithdrawalRequest ---
    @Override
    @Transactional
    // Explicitly use the DTO in the method signature to match the Interface
    public WithdrawalDTO requestWithdrawal(UUID userId, lk.freelance.backend.dto.WithdrawalRequest request) {
        Wallet wallet = walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", userId));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Explicitly use the Entity for saving
        lk.freelance.backend.entity.WithdrawalRequest entity = lk.freelance.backend.entity.WithdrawalRequest.builder()
                .user(user)
                .amount(request.getAmount())
                .status("PENDING")
                .bankAccountDetails(Map.of(
                        "bankName", request.getBankName(),
                        "accountNumber", request.getAccountNumber(),
                        "accountName", request.getAccountName()
                ))
                .requestedAt(LocalDateTime.now())
                .build();

        return withdrawalMapper.toDTO(withdrawalRequestRepository.save(entity));
    }

    @Override
    public List<WithdrawalDTO> getWithdrawalHistory(UUID userId) {
        return withdrawalRequestRepository.findByUser_UserId(userId).stream()
                .map(withdrawalMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(lk.freelance.backend.enums.UserStatus.SUSPENDED);
        userRepository.save(user);
    }
}