package org.service.event.global

class EventNotFoundException(message: String = "해당 행사를 찾을 수 없습니다.") : RuntimeException(message)
class MemberNotFoundException(message: String = "사용자 정보를 찾을 수 없습니다.") : RuntimeException(message)
class DuplicateLikeException(message: String = "이미 찜한 행사입니다.") : RuntimeException(message)
class LikeNotFoundException(message: String = "찜한 기록이 존재하지 않습니다.") : RuntimeException(message)
