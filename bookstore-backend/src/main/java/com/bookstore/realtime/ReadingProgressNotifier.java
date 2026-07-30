package com.bookstore.realtime;

import com.bookstore.library.event.ReadingProgressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadingProgressNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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