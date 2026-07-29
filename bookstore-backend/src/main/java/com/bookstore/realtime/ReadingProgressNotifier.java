package com.bookstore.realtime;

import com.bookstore.library.event.ReadingProgressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadingProgressNotifier {

    private static final Logger log = LoggerFactory.getLogger(ReadingProgressNotifier.class);

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onReadingProgressUpdated(ReadingProgressUpdatedEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.userId().toString(),
                    "/queue/reading-progress",
                    event
            );
        } catch (Exception ex) {
            log.warn("Failed to push reading progress update via WebSocket for user {}: {}",
                    event.userId(), ex.getMessage());
        }
    }
}