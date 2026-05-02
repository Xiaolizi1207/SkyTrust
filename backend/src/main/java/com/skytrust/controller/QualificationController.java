package com.skytrust.controller;

import com.skytrust.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qualification")
public class QualificationController {

    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        String company = (String) body.get("company");
        String contact = (String) body.get("contact");
        String phone = (String) body.get("phone");
        String email = (String) body.get("email");
        String scale = (String) body.get("scale");
        String reason = (String) body.get("reason");

        if (company == null || company.isBlank()
                || contact == null || contact.isBlank()
                || phone == null || phone.isBlank()
                || email == null || email.isBlank()) {
            return Result.error("请填写所有必填字段");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("company", company);
        result.put("status", "submitted");
        result.put("message", "申请已提交，审核通过后会将邀请码发送至您的邮箱");
        return Result.success(result, "申请提交成功");
    }
}
