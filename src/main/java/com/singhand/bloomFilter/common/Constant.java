package com.singhand.bloomFilter.common;

public class Constant {

    public static final long EXPIRATION_TIME = 432_000_000;     // 5 days
    public static final String SECRET = "CodeSheepSecret";      // JWT password
    public static final String TOKEN_PREFIX = "Bearer";         // Token prefix
    public static final String HEADER_STRING = "Authorization"; // Header Key which store Token

}
