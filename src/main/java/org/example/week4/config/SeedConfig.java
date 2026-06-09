package org.example.week4.config;

import lombok.RequiredArgsConstructor;
import org.example.week4.loader.CommentSeedLoader;
import org.example.week4.loader.FileSeedLoader;
import org.example.week4.loader.LikeSeedLoader;
import org.example.week4.loader.PostSeedLoader;
import org.example.week4.loader.ReportSeedLoader;
import org.example.week4.loader.UserSeedLoader;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("development")
@RequiredArgsConstructor
public class SeedConfig {
    private final FileSeedLoader fileSeedLoader;
    private final UserSeedLoader userSeedLoader;
    private final PostSeedLoader postSeedLoader;
    private final CommentSeedLoader commentSeedLoader;
    private final LikeSeedLoader likeSeedLoader;
    private final ReportSeedLoader reportSeedLoader;

    @Bean
    ApplicationRunner seedRunner() {
        return args -> {
            fileSeedLoader.load();
            userSeedLoader.load();
            postSeedLoader.load();
            commentSeedLoader.load();
            likeSeedLoader.load();
            reportSeedLoader.load();
        };
    }
}
