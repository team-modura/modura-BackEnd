package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User")
@Validated
public class UserController {

    private final UserQueryService userQueryService;
}