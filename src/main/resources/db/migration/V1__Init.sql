CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `created_at` datetime(6) NOT NULL,
                            `updated_at` datetime(6) NOT NULL,
                            `name` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`)
);

CREATE TABLE `content` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `created_at` datetime(6) NOT NULL,
                           `updated_at` datetime(6) NOT NULL,
                           `plot` varchar(255) DEFAULT NULL,
                           `thumbnail` text,
                           `title_eng` varchar(255) NOT NULL,
                           `title_kr` varchar(255) NOT NULL,
                           `year` int DEFAULT NULL,
                           PRIMARY KEY (`id`)
);

CREATE TABLE `content_category` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `created_at` datetime(6) NOT NULL,
                                    `updated_at` datetime(6) NOT NULL,
                                    `category_id` bigint NOT NULL,
                                    `content_id` bigint NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UQ_CONTENT_CATEGORY_ID` (`content_id`,`category_id`),
                                    KEY `FKqp99yhrvthtx1pdo1c1xt0k7j` (`category_id`),
                                    CONSTRAINT `FKqp99yhrvthtx1pdo1c1xt0k7j` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
                                    CONSTRAINT `FKs8wtlro1qiot5apyxafqbremc` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`)
);

CREATE TABLE `content_likes` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `created_at` datetime(6) NOT NULL,
                                 `updated_at` datetime(6) NOT NULL,
                                 `content_id` bigint NOT NULL,
                                 `user_id` bigint NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UQ_USER_CONTENT_ID` (`user_id`,`content_id`),
                                 KEY `FK3phynxqdk3ogejthkee6m3tr4` (`content_id`),
                                 CONSTRAINT `FK3phynxqdk3ogejthkee6m3tr4` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`),
                                 CONSTRAINT `FKtq9ln30we2d1nbibcevn56cxi` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `content_platform` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `created_at` datetime(6) NOT NULL,
                                    `updated_at` datetime(6) NOT NULL,
                                    `content_id` bigint NOT NULL,
                                    `platform_id` bigint NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UQ_CONTENT_PLATFORM_ID` (`content_id`,`platform_id`),
                                    KEY `FKj3tbex62q91h4q1kk0fh7y340` (`platform_id`),
                                    CONSTRAINT `FK81pxeikp60wcq4iiuergroet5` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`),
                                    CONSTRAINT `FKj3tbex62q91h4q1kk0fh7y340` FOREIGN KEY (`platform_id`) REFERENCES `platform` (`id`)
);

CREATE TABLE `content_review` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime(6) NOT NULL,
                                  `updated_at` datetime(6) NOT NULL,
                                  `body` varchar(255) NOT NULL,
                                  `rating` int NOT NULL,
                                  `content_id` bigint NOT NULL,
                                  `user_id` bigint NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FK9yoxseur7ctslf98xdf5js26t` (`content_id`),
                                  KEY `FKo9dobo471hio3mvk68qkpoojh` (`user_id`),
                                  CONSTRAINT `FK9yoxseur7ctslf98xdf5js26t` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`),
                                  CONSTRAINT `FKo9dobo471hio3mvk68qkpoojh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `place` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) NOT NULL,
                         `updated_at` datetime(6) NOT NULL,
                         `name` varchar(255) NOT NULL,
                         `content_id` bigint NOT NULL,
                         PRIMARY KEY (`id`),
                         KEY `FKmbcbqlq3e4w6aegphgtnet8yt` (`content_id`),
                         CONSTRAINT `FKmbcbqlq3e4w6aegphgtnet8yt` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`)
);

CREATE TABLE `place_likes` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `created_at` datetime(6) NOT NULL,
                               `updated_at` datetime(6) NOT NULL,
                               `place_id` bigint NOT NULL,
                               `user_id` bigint NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `UQ_USER_PLACE_ID` (`user_id`,`place_id`),
                               KEY `FKfcoiwjx2egm74tf3s0rn3tl2x` (`place_id`),
                               CONSTRAINT `FK17gfej5xoh7g7qm0j3mo7al85` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                               CONSTRAINT `FKfcoiwjx2egm74tf3s0rn3tl2x` FOREIGN KEY (`place_id`) REFERENCES `place` (`id`)
);

CREATE TABLE `place_review` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_at` datetime(6) NOT NULL,
                                `updated_at` datetime(6) NOT NULL,
                                `body` varchar(255) NOT NULL,
                                `rating` int NOT NULL,
                                `place_id` bigint NOT NULL,
                                `user_id` bigint NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FK8as5agwrso2psutsrsfgn486u` (`place_id`),
                                KEY `FKet8vy13m7akh641174y4dn8v5` (`user_id`),
                                CONSTRAINT `FK8as5agwrso2psutsrsfgn486u` FOREIGN KEY (`place_id`) REFERENCES `place` (`id`),
                                CONSTRAINT `FKet8vy13m7akh641174y4dn8v5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `platform` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `created_at` datetime(6) NOT NULL,
                            `updated_at` datetime(6) NOT NULL,
                            `image_url` text NOT NULL,
                            `name` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`)
);

CREATE TABLE `popular_keyword` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `created_at` datetime(6) NOT NULL,
                                   `updated_at` datetime(6) NOT NULL,
                                   `count` bigint NOT NULL,
                                   `keyword` varchar(255) NOT NULL,
                                   PRIMARY KEY (`id`)
);

CREATE TABLE `review_image` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_at` datetime(6) NOT NULL,
                                `updated_at` datetime(6) NOT NULL,
                                `image_url` text NOT NULL,
                                `place_review_id` bigint NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FKo5idjtruc4rm0me3u8l3fk9ge` (`place_review_id`),
                                CONSTRAINT `FKo5idjtruc4rm0me3u8l3fk9ge` FOREIGN KEY (`place_review_id`) REFERENCES `place_review` (`id`)
);

CREATE TABLE `terms` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) NOT NULL,
                         `updated_at` datetime(6) NOT NULL,
                         `body` varchar(255) DEFAULT NULL,
                         `optional` bit(1) DEFAULT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `user_terms` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `created_at` datetime(6) NOT NULL,
                              `updated_at` datetime(6) NOT NULL,
                              `terms_id` bigint NOT NULL,
                              `user_id` bigint NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `FKsv90iyco7g8yl9kbml0m5pjhl` (`terms_id`),
                              KEY `FKn125t6gpo973wja3uarq4kd5f` (`user_id`),
                              CONSTRAINT `FKn125t6gpo973wja3uarq4kd5f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                              CONSTRAINT `FKsv90iyco7g8yl9kbml0m5pjhl` FOREIGN KEY (`terms_id`) REFERENCES `terms` (`id`)
);

CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) NOT NULL,
                         `updated_at` datetime(6) NOT NULL,
                         `birth` varchar(255) NOT NULL,
                         `gender` enum('FEMALE','MALE','NONE') NOT NULL,
                         `image` text,
                         `inactive_date` date DEFAULT NULL,
                         `name` varchar(255) NOT NULL,
                         `nickname` varchar(255) NOT NULL,
                         `oauth_id` varchar(255) DEFAULT NULL,
                         `phone` varchar(13) NOT NULL,
                         PRIMARY KEY (`id`)
);