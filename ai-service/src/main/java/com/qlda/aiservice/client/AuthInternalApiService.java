package com.qlda.aiservice.client;

public interface AuthInternalApiService {
    boolean hasSystemStatisticPermission(Long userId);

    long getTotalUserCount(Long userId);
}
