package com.example.backend.controller;

import com.example.backend.bean.*;
import com.example.backend.entity.User;
import com.example.backend.service.UserManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserManagerController {

    private final UserManagerService userManagerService;

    @PostMapping("/register")
    public Result<UserLoginResponse> register(@RequestBody UserRegisterRequest user) {
        User register = userManagerService.register(user);
        UserLoginResponse response = UserLoginResponse.fromEntity(register);
        return Result.success(response);
    }

    @GetMapping("/check/username/{username}")
    public Result<Void> checkUsername(@PathVariable String username) {
        if (userManagerService.isUsernameExist(username)) {
            return Result.error(500, "用户名已存在");
        }
        return Result.success();
    }

    @GetMapping("/check/email/{email}")
    public Result<Void> checkMail(@PathVariable String email) {
        if (userManagerService.isEmailExist(email)) {
            return Result.error(500, "邮箱已存在");
        }
        return Result.success();
    }

    @GetMapping("/check/code")
    public Result<Void> sendMailCode(@RequestParam String email) {
        userManagerService.sendMailCode(email);
        return Result.success();
    }

    @PostMapping("/check/code")
    public Result<Void> checkMail(@RequestParam MailCodeCheckRequest request) {
        if (userManagerService.isEmailCodeMatched(request.getEmail(), request.getCode())) {
            return Result.success();
        }
        return Result.error(500, "邮箱验证码错误");
    }

    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody UserLoginRequest user) {
        User login = userManagerService.login(user);
        UserLoginResponse response = UserLoginResponse.fromEntity(login);
        return Result.success(response);
    }

    @GetMapping("/users/{id}")
    public Result<UserLoginResponse> getUser(@PathVariable Long id) {
        // TODO
        return Result.error(500, "未实现");
    }

    @PostMapping("/users/{id}")
    public Result<UserLoginResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest user) {
        User update = userManagerService.update(id, user);
        UserLoginResponse response = UserLoginResponse.fromEntity(update);
        return Result.success(response);
    }

    @DeleteMapping("/users/{id}")
    public Result<UserLoginResponse> removeUser(@PathVariable Long id) {
        // TODO
        return Result.error(500, "未实现");
    }

    @GetMapping("/forget")
    public Result<Void> forgetPassword(@RequestParam String email) {
        userManagerService.forgetPassword(email);
        return Result.success();
    }

    @PostMapping("/reset")
    public Result<Void> resetPassword(@RequestParam PasswordResetRequest request) {
        userManagerService.resetPassword(request);
        return Result.success();
    }

    @GetMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
