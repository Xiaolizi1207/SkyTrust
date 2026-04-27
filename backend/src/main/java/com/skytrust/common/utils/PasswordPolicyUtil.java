package com.skytrust.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码策略工具类
 * 验证密码是否符合配置的强度策略
 *
 * @author SkyTrust Team
 */
@Component
@ConfigurationProperties(prefix = "skytrust.password-policy")
@Data
public class PasswordPolicyUtil {

    /** 最小长度 */
    private int minLength = 8;

    /** 最大长度 */
    private int maxLength = 100;

    /** 是否要求大写字母 */
    private boolean requireUppercase = true;

    /** 是否要求小写字母 */
    private boolean requireLowercase = true;

    /** 是否要求数字 */
    private boolean requireDigit = true;

    /** 是否要求特殊字符 */
    private boolean requireSpecialChar = true;

    /** 允许的最大连续重复字符数（0表示不限制） */
    private int maxConsecutiveRepeats = 3;

    /** 是否禁止包含用户名 */
    private boolean forbidUsername = true;

    /** 最小特殊字符数 */
    private int minSpecialChars = 1;

    /** 特殊字符集 */
    private String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";

    // 编译后的正则
    private Pattern uppercasePattern;
    private Pattern lowercasePattern;
    private Pattern digitPattern;
    private Pattern specialCharPattern;

    @PostConstruct
    public void init() {
        uppercasePattern = Pattern.compile(".*[A-Z].*");
        lowercasePattern = Pattern.compile(".*[a-z].*");
        digitPattern = Pattern.compile(".*\\d.*");
        specialCharPattern = Pattern.compile(".*[" + Pattern.quote(specialChars) + "].*");
    }

    /**
     * 密码校验结果
     */
    @Data
    public static class PasswordValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }

    /**
     * 验证密码强度
     *
     * @param password 密码原文
     * @param username 用户名（用于检查密码是否包含用户名）
     * @return 验证结果
     */
    public PasswordValidationResult validate(String password, String username) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.isEmpty()) {
            result.addError("密码不能为空");
            return result;
        }

        // 长度检查
        if (password.length() < minLength) {
            result.addError("密码长度不能少于" + minLength + "个字符");
        }
        if (password.length() > maxLength) {
            result.addError("密码长度不能超过" + maxLength + "个字符");
        }

        // 大写字母检查
        if (requireUppercase && !uppercasePattern.matcher(password).matches()) {
            result.addError("密码必须包含至少一个大写字母");
        }

        // 小写字母检查
        if (requireLowercase && !lowercasePattern.matcher(password).matches()) {
            result.addError("密码必须包含至少一个小写字母");
        }

        // 数字检查
        if (requireDigit && !digitPattern.matcher(password).matches()) {
            result.addError("密码必须包含至少一个数字");
        }

        // 特殊字符检查
        if (requireSpecialChar) {
            int specialCount = 0;
            for (char c : password.toCharArray()) {
                if (specialChars.indexOf(c) >= 0) {
                    specialCount++;
                }
            }
            if (specialCount < minSpecialChars) {
                result.addError("密码必须包含至少" + minSpecialChars + "个特殊字符");
            }
        }

        // 连续重复字符检查
        if (maxConsecutiveRepeats > 0) {
            int maxRepeat = 1;
            int currentRepeat = 1;
            for (int i = 1; i < password.length(); i++) {
                if (password.charAt(i) == password.charAt(i - 1)) {
                    currentRepeat++;
                    maxRepeat = Math.max(maxRepeat, currentRepeat);
                } else {
                    currentRepeat = 1;
                }
            }
            if (maxRepeat > maxConsecutiveRepeats) {
                result.addError("密码中不允许有超过" + maxConsecutiveRepeats + "个连续重复字符");
            }
        }

        // 禁止包含用户名
        if (forbidUsername && username != null && !username.isEmpty()) {
            String lowerPassword = password.toLowerCase();
            String lowerUsername = username.toLowerCase();
            if (lowerPassword.contains(lowerUsername)) {
                result.addError("密码不能包含用户名");
            }
        }

        return result;
    }

    /**
     * 快速验证，返回错误消息（null表示通过）
     */
    public String validateAndGetMessage(String password, String username) {
        PasswordValidationResult result = validate(password, username);
        return result.isValid() ? null : result.getFirstError();
    }
}
