import { updateProgress } from "../features/library/libraryApi";
import { getPendingProgress, markSynced } from "./offlineProgressStore";

let isFlushing = false;

export async function flushPendingProgress(): Promise<void> {
    if (isFlushing) return;
    isFlushing = true;
    try {
        const pending = await getPendingProgress();
        for (const record of pending) {
            try {
                await updateProgress(record.variantId, {
                    position: record.position,
                    playbackSpeed: record.playbackSpeed,
                    clientSessionId: null,
                });
                await markSynced(record.variantId);
            } catch {
                // Vẫn còn mất mạng hoặc lỗi khác - giữ nguyên "pending", thử lại ở lần flush sau
            }
        }
    } finally {
        isFlushing = false;
    }
}

export function registerOfflineSyncListeners(): () => void {
    function handleOnline() {
        flushPendingProgress();
    }
    window.addEventListener("online", handleOnline);
    return () => window.removeEventListener("online", handleOnline);
}