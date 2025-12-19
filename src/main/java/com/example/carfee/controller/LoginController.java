package com.example.carfee.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        Map<String, Object> res = new HashMap<>();

        // 固定账号
        if ("gao".equals(username) && "123456".equals(password)) {
            session.setAttribute("user", username);
            res.put("success", true);
            return res;
        }

        res.put("success", false);
        res.put("message", "用户名或密码错误");
        return res;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return res;
    }
}
