package com.grepp.nbe1_3_team9.common.exception.exceptions

import com.grepp.nbe1_3_team9.common.exception.BaseException
import com.grepp.nbe1_3_team9.common.exception.ExceptionMessage

class TokenException(message: ExceptionMessage) : BaseException(message.text)