package com.chat.domain.common;

import com.fasterxml.uuid.Generators;

/**
 * UUID v7 생성 유틸
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String generate() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }
}
