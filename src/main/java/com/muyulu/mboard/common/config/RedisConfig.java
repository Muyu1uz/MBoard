package com.muyulu.mboard.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {

    @Bean
    public DefaultRedisScript<java.util.List> ratingUpdateScript() throws Exception {
        DefaultRedisScript<java.util.List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/rating_update.lua"));
        script.setResultType(java.util.List.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<java.util.List> ratingRollbackScript() {
        DefaultRedisScript<java.util.List> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local userHashKey = KEYS[1]
                local aggKey = KEYS[2]
                local hotKey = KEYS[3]
                local userId = ARGV[1]
                local oldStar = tonumber(ARGV[2])
                local newStar = tonumber(ARGV[3])
                local hotDelta = tonumber(ARGV[4])
                local current = redis.call('HGET', userHashKey, userId)
                if current == false then
                  return {-1}
                end
                local count = tonumber(redis.call('HGET', aggKey, 'count') or '0')
                local sum = tonumber(redis.call('HGET', aggKey, 'sum') or '0')
                if oldStar == 0 then
                  redis.call('HDEL', userHashKey, userId)
                  count = math.max(0, count - 1)
                  sum = sum - newStar
                else
                  redis.call('HSET', userHashKey, userId, oldStar)
                  sum = sum - newStar + oldStar
                end
                local avg = 0
                if count > 0 then
                  avg = sum / count
                end
                redis.call('HSET', aggKey, 'count', count, 'sum', sum, 'avg', avg)
                if hotDelta ~= 0 then
                  redis.call('ZINCRBY', hotKey, -hotDelta, string.match(aggKey, '(%d+)$'))
                end
                return {oldStar, count, sum, avg}
                """);
        script.setResultType(java.util.List.class);
        return script;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
