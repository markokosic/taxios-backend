package com.markokosic.minicrm.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TemporaryPasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_-+=";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final int DEFAULT_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPasswordGenerator() {
    }

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }

        List<Character> characters = new ArrayList<>();
        characters.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        characters.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        characters.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        characters.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            characters.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(characters, RANDOM);

        StringBuilder password = new StringBuilder(length);
        for (char c : characters) {
            password.append(c);
        }
        return password.toString();
    }
}
