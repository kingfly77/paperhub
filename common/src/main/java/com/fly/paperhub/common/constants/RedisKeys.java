package com.fly.paperhub.common.constants;

import com.fly.paperhub.common.utils.StringUtil;

public final class RedisKeys {

    private static final String PROJECT = "paperhub";

    private static final String USER_MODULE = "user";
    private static final String PAPER_MODULE = "paper";
    private static final String NOTE_MODULE = "note";
    private static final String GATEWAY_MODULE = "gateway";

    public static final String CONNECTOR = ":";

    public static final String TOKEN_PREFIX = PROJECT + CONNECTOR + USER_MODULE + CONNECTOR + "token";

    public static final String NOTE_PREFIX = PROJECT + CONNECTOR + NOTE_MODULE + CONNECTOR + "note";
}
