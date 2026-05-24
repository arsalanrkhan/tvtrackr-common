package com.tvtrackr.common.redis.service.impl;

import com.tvtrackr.common.redis.service.RedisService;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public String getAndDeleteString(String key) {
    return stringRedisTemplate.opsForValue().getAndDelete(key);
  }

  @Override
  public void save(String key, Object value, long ttlMs) {
    redisTemplate.opsForValue().set(key, value, ttlMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean saveIfAbsent(String key, Object value, long ttlMs) {
    Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(ttlMs));
    return Boolean.TRUE.equals(result);
  }

  @Override
  public void saveString(String key, String value, long ttlMs) {
    stringRedisTemplate.opsForValue().set(key, value, ttlMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean saveStringIfAbsent(String key, String value, long ttlMs) {
    Boolean result =
        stringRedisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(ttlMs));
    return Boolean.TRUE.equals(result);
  }

  @Override
  public void saveStringPermanent(String key, String value) {
    stringRedisTemplate.opsForValue().set(key, value);
  }

  @Override
  public <T> T get(String key, Class<T> type) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) {
      return null;
    }
    return type.cast(value);
  }

  @Override
  public String getString(String key) {
    return stringRedisTemplate.opsForValue().get(key);
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  @Override
  public boolean exists(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  @Override
  public void setBit(String key, long offset) {
    redisTemplate.opsForValue().setBit(key, offset, true);
  }

  @Override
  public boolean getBit(String key, long offset) {
    return redisTemplate.opsForValue().getBit(key, offset);
  }

  @Override
  public List<Boolean> getBitsPipelined(String key, long[] offsets) {
    List<Object> results =
        redisTemplate.executePipelined(
            (RedisCallback<Object>)
                connection -> {
                  byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                  for (long offset : offsets) {
                    connection.stringCommands().getBit(keyBytes, offset);
                  }
                  return null;
                });
    return results.stream().map(Boolean.TRUE::equals).collect(Collectors.toList());
  }

  @Override
  public void setBitsPipelined(String key, long[] offsets) {
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
              for (long offset : offsets) {
                connection.stringCommands().setBit(keyBytes, offset, true);
              }
              return null;
            });
  }
}
