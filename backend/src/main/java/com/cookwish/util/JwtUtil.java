package com.cookwish.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public final class JwtUtil {
    private static final String SECRET = "CookWish-Demo-Secret-Change-Before-Deployment-2026";
    private JwtUtil() {}
    private static String b64(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private static byte[] sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
    public static String createToken(String username, String role) throws Exception {
        String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        long expiry = Instant.now().getEpochSecond() + 60L * 60L * 8L;
        String payload = b64(("{\"sub\":\"" + username + "\",\"role\":\"" + role + "\",\"exp\":" + expiry + "}").getBytes(StandardCharsets.UTF_8));
        String content = header + "." + payload;
        return content + "." + b64(sign(content));
    }
    public static Claims validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String content = parts[0] + "." + parts[1];
            byte[] expected = sign(content);
            byte[] actual = Base64.getUrlDecoder().decode(parts[2]);
            if (!java.security.MessageDigest.isEqual(expected, actual)) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String sub = extract(payload, "sub");
            String role = extract(payload, "role");
            String expText = extract(payload, "exp");
            if (sub == null || role == null || expText == null || Long.parseLong(expText) < Instant.now().getEpochSecond()) return null;
            return new Claims(sub, role);
        } catch (Exception e) { return null; }
    }
    private static String extract(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+))").matcher(json);
        if (!matcher.find()) return null;
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }
    public static class Claims {
        public final String username;
        public final String role;
        public Claims(String username, String role) { this.username = username; this.role = role; }
    }
}
