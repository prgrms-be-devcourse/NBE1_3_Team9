package com.grepp.nbe1_3_team9.domain.service.user

import com.grepp.nbe1_3_team9.admin.jwt.CookieUtil
import com.grepp.nbe1_3_team9.admin.jwt.JwtUtil
import com.grepp.nbe1_3_team9.admin.jwt.TokenRes
import com.grepp.nbe1_3_team9.admin.service.CustomUserDetails
import com.grepp.nbe1_3_team9.common.exception.ExceptionMessage
import com.grepp.nbe1_3_team9.common.exception.exceptions.UserException
import com.grepp.nbe1_3_team9.controller.user.dto.*
import com.grepp.nbe1_3_team9.domain.entity.user.Role
import com.grepp.nbe1_3_team9.domain.entity.user.User
import com.grepp.nbe1_3_team9.domain.repository.group.membership.GroupMembershipRepository
import com.grepp.nbe1_3_team9.domain.repository.user.UserRepository
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val groupMembershipRepository: GroupMembershipRepository
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    // 회원가입
    @Transactional
    fun register(signUpReq: SignUpReq): User {
        if (userRepository.findByEmail(signUpReq.email).isPresent) {
            throw UserException(ExceptionMessage.USER_IS_PRESENT)
        }

        val encodedPassword = passwordEncoder.encode(signUpReq.password)
        val user = User(
            username = signUpReq.username,
            email = signUpReq.email,
            password = encodedPassword,
            role = Role.MEMBER
        )

        return userRepository.save(user)
    }

    // 로그인
    fun signIn(signInReq: SignInReq, response: HttpServletResponse): TokenRes {
        val user = findByEmailOrThrowUserException(signInReq.email)
        if (!passwordEncoder.matches(signInReq.password, user.password)) {
            throw UserException(ExceptionMessage.USER_LOGIN_FAIL)
        }

        user.updateLastLoginDate()
        userRepository.save(user)

        val userDetails = CustomUserDetails(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        return jwtUtil.generateToken(authentication, response)
    }

    // 로그아웃
    fun logout(authentication: Authentication, response: HttpServletResponse) {
        jwtUtil.deleteTokens(authentication, response)
    }

    // 회원 정보 조회
    fun getUser(userId: Long): User {
        return findByIdOrThrowUserException(userId)
    }

    // 회원 정보 수정
    @Transactional
    fun updateProfile(targetUserId: Long, updateProfileReq: UpdateProfileReq) {
        val loggedInUserId = getAuthenticatedUserId()
        checkAuthorization(loggedInUserId, targetUserId)

        val user = findByIdOrThrowUserException(targetUserId)
        user.updateProfile(updateProfileReq.username, updateProfileReq.email)
        log.info("사용자 정보가 수정되었습니다. userId: {}", targetUserId)
    }

    // 비밀번호 변경
    @Transactional
    fun changePassword(targetUserId: Long, changePasswordReq: ChangePasswordReq) {
        val loggedInUserId = getAuthenticatedUserId()
        checkAuthorization(loggedInUserId, targetUserId)

        val user = findByIdOrThrowUserException(targetUserId)
        if (!passwordEncoder.matches(changePasswordReq.currentPassword, user.password)) {
            throw IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.")
        }

        val encodedPassword = passwordEncoder.encode(changePasswordReq.newPassword)
        user.changePassword(encodedPassword)
        log.info("사용자 비밀번호가 수정되었습니다. userId: {}", targetUserId)
    }

    // 회원 정보 삭제
    @Transactional
    fun deleteUser(targetUserId: Long, response: HttpServletResponse) {
        val authentication = SecurityContextHolder.getContext().authentication
        val loggedInUserId = (authentication.principal as? CustomUserDetails)?.getUserId()
            ?: throw UserException(ExceptionMessage.UNAUTHORIZED_ACTION)

        checkAuthorization(loggedInUserId, targetUserId)

        val user = findByIdOrThrowUserException(targetUserId)
        groupMembershipRepository.deleteByUserId(targetUserId)
        userRepository.delete(user)
        log.info("사용자 정보가 삭제되었습니다. userId: {}", targetUserId)

        CookieUtil.deleteAccessTokenCookie(response)
        CookieUtil.deleteRefreshTokenCookie(response)
        SecurityContextHolder.clearContext()
    }

    private fun findByIdOrThrowUserException(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            log.warn(">>>> {} : {} <<<<", userId, ExceptionMessage.USER_NOT_FOUND)
            UserException(ExceptionMessage.USER_NOT_FOUND)
        }
    }

    private fun findByEmailOrThrowUserException(email: String): User {
        return userRepository.findByEmail(email).orElseThrow {
            log.warn(">>>> {} : {} <<<<", email, ExceptionMessage.USER_NOT_FOUND)
            UserException(ExceptionMessage.USER_NOT_FOUND)
        }
    }

    // 권한 검증 메서드
    fun checkAuthorization(loggedInUserId: Long, targetUserId: Long) {
        if (loggedInUserId != targetUserId) {
            throw UserException(ExceptionMessage.UNAUTHORIZED_ACTION)
        }
    }

    // 사용자 ID 확인 로직
    fun getAuthenticatedUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        return (authentication.principal as? CustomUserDetails)?.getUserId()
            ?: throw UserException(ExceptionMessage.UNAUTHORIZED_ACTION)
    }

    // 현재 사용자 정보를 가져오는 메서드
    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw UserException(ExceptionMessage.UNAUTHORIZED_ACTION)
        }

        val email = (authentication.principal as? CustomUserDetails)?.getUserEmail()
            ?: throw UserException(ExceptionMessage.USER_NOT_FOUND)

        return userRepository.findByEmail(email).orElseThrow {
            UserException(ExceptionMessage.USER_NOT_FOUND)
        }
    }

    fun getCurrentUserInfo(): UserInfoResp {
        val user = getCurrentUser()
        return UserInfoResp(
            userId = user.userId,
            username = user.username,
            email = user.email,
            role = user.role.name,
            joinedDate = user.signUpDate
        )
    }
}
