package com.modura.modura_server.domain.announcement.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
@Tag(name = "Announcement")
@Validated
public class AnnouncementController {

}