package com.zyj.warlock.core;

import com.zyj.warlock.enums.LockType;
import lombok.Data;

@Data
public class LockInfo {
    private String lockKey;
    private LockType lockType;
}
