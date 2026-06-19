package users;

public record SessionValidationResponse(SessionStatus status, RedisSessionData redisSessionData) {

}