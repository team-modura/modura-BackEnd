package com.modura.modura_server.domain.announcement.repository;

import com.modura.modura_server.domain.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

}
