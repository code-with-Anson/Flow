package com.flow.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class AESUtils {

    /**
     * 加密前缀标识，用于判断内容是否已加密
     */
    public static final String ENCRYPTED_PREFIX = "ENC:";

    @Value("${flow.security.aes-secret:FlowTech_1004_Key}")
    private String secretKey;

    private static final String ALGORITHM = "AES";

    /**
     * 使用 SHA-256 派生密钥，确保正好 16 字节 (AES-128)
     */
    private SecretKeySpec deriveKey() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            // 取前 16 字节用于 AES-128
            return new SecretKeySpec(Arrays.copyOf(key, 16), ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }

    /**
     * 加密内容，自动添加前缀标识
     */
    public String encrypt(String content) {
        if (content == null) {
            return null;
        }
        // 已加密则直接返回
        if (isEncrypted(content)) {
            return content;
        }
        try {
            SecretKeySpec key = deriveKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            byte[] result = cipher.doFinal(byteContent);
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            log.error("AES Encrypt Error", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 解密内容，自动处理前缀
     */
    public String decrypt(String content) {
        if (content == null) {
            return null;
        }
        // 未加密则直接返回原文
        if (!isEncrypted(content)) {
            return content;
        }
        try {
            // 移除前缀
            String encrypted = content.substring(ENCRYPTED_PREFIX.length());
            SecretKeySpec key = deriveKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES Decrypt Error", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * 判断内容是否已加密
     */
    public boolean isEncrypted(String content) {
        return content != null && content.startsWith(ENCRYPTED_PREFIX);
    }
}
