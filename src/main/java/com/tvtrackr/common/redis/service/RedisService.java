package com.tvtrackr.common.redis.service;

import java.util.List;

public interface RedisService {

  String getAndDeleteString(String key);

  void save(String key, Object value, long ttlMs);

  boolean saveIfAbsent(String key, Object value, long ttlMs);

  void saveString(String key, String value, long ttlMs);

  boolean saveStringIfAbsent(String key, String value, long ttlMs);

  void saveStringPermanent(String key, String value);

  <T> T get(String key, Class<T> type);

  String getString(String key);

  void delete(String key);

  boolean exists(String key);

  void setBit(String key, long offset);

  boolean getBit(String key, long offset);

  List<Boolean> getBitsPipelined(String key, long[] offsets);

  void setBitsPipelined(String key, long[] offsets);
}
