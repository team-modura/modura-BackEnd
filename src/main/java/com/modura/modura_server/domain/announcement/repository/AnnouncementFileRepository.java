package com.modura.modura_server.domain.announcement.repository;

import com.modura.modura_server.domain.announcement.entity.AnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementFileRepository extends JpaRepository<AnnouncementFile, Long> {

}
