package com.grepp.nbe1_3_team9.controller.event.dto

import jakarta.validation.constraints.*
import java.time.LocalDate

data class UpdateEventRequest(
    @field:NotBlank(message = "일정 이름 필수 입력")
    @field:Size(min = 2, max = 50, message = "일정 이름은 2글자 이상 50글자 미만으로 해야합니다")
    val eventName: String,

    @field:Size(max = 500, message = "설명은 최대 500자까지 허용됩니다")
    val description: String? = null,

    @field:NotNull(message = "시작 날짜 선택 필수")
    @field:FutureOrPresent(message = "시작일은 오늘날짜 이후부터 가능합니다")
    val startDate: LocalDate,

    @field:NotNull(message = "종료 날짜 선택 필수")
    @field:Future(message = "종료일은 오늘이 지난 날부터 가능합니다.")
    val endDate: LocalDate
) {
    @AssertTrue(message = "종료일은 시작일과 같거나 이후여야 합니다")
    fun isEndDateValid(): Boolean {
        return endDate != null && startDate != null && endDate.isAfter(startDate)
    }
}
