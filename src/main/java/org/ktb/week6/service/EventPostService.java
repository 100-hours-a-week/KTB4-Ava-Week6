package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.entity.EventPost;
import org.ktb.week6.entity.Post;
import org.ktb.week6.repository.EventPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventPostService {
    private final EventPostRepository eventPostRepository;

    @Transactional
    public EventPost createEventPost(Post post, int capacity, LocalDateTime deadline) {
        EventPost eventPost = new EventPost(post, capacity, deadline);
        return eventPostRepository.save(eventPost);
    }

}
