import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getStoredToken, API_BASE_URL } from "./apiClient";

export interface ReadingProgressUpdate {
    userId: string;
    productVariantId: string;
    position: string;
    playbackSpeed: number | null;
    updatedAt: string;
    clientSessionId: string | null;
}

let client: Client | null = null;
let isConnected = false;
const pendingCallbacks: Array<() => void> = [];

function ensureClient(): Client {
    if (client) return client;

    client = new Client({
        webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
        connectHeaders: {
            Authorization: `Bearer ${getStoredToken() ?? ""}`,
        },
        reconnectDelay: 5000,
    });

    client.onConnect = () => {
        isConnected = true;
        pendingCallbacks.splice(0).forEach((cb) => cb());
    };

    client.onDisconnect = () => {
        isConnected = false;
    };

    client.activate();
    return client;
}

export function subscribeReadingProgress(
    onUpdate: (update: ReadingProgressUpdate) => void
): () => void {
    const c = ensureClient();
    let subscription: { unsubscribe: () => void } | null = null;

    function doSubscribe() {
        subscription = c.subscribe("/user/queue/reading-progress", (message: IMessage) => {
            onUpdate(JSON.parse(message.body) as ReadingProgressUpdate);
        });
    }

    if (isConnected) {
        doSubscribe();
    } else {
        pendingCallbacks.push(doSubscribe);
    }

    return () => {
        subscription?.unsubscribe();
    };
}