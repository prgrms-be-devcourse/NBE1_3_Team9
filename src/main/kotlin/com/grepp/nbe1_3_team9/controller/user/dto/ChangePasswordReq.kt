package com.grepp.nbe1_3_team9.controller.user.dto

data class ChangePasswordReq(
    val currentPassword: String = "",
    val newPassword: String = ""
)