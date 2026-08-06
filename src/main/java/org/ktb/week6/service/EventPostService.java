package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.entity.EventPost;
import org.ktb.week6.entity.Post;
import org.ktb.week6.repository.EventApplicationRepository;
import org.ktb.week6.repository.EventPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventPostService {
    private final EventPostRepository eventPostRepository;
    private final EventApplicationRepository eventApplicationRepository;

    @Transactional
    public void createEventPost(Post post, int capacity, LocalDateTime deadline) {
        EventPost eventPost = new EventPost(post, capacity, deadline);
        eventPostRepository.save(eventPost);

    }

}
