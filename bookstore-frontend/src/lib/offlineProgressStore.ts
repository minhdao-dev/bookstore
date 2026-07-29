const DB_NAME = "bookstore-offline";
const DB_VERSION = 1;
const STORE_NAME = "reading-progress";

export interface LocalProgressRecord {
    variantId: string;
    position: string;
    playbackSpeed: number | null;
    updatedAt: number;
    synced: boolean;
}

function openDb(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION);
        request.onupgradeneeded = () => {
            const db = request.result;
            if (!db.objectStoreNames.contains(STORE_NAME)) {
                db.createObjectStore(STORE_NAME, { keyPath: "variantId" });
            }
        };
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
}

export async function saveLocalProgress(
    variantId: string,
    position: string,
    playbackSpeed: number | null
): Promise<void> {
    const db = await openDb();
    return new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, "readwrite");
        const store = tx.objectStore(STORE_NAME);
        const record: LocalProgressRecord = {
            variantId,
            position,
            playbackSpeed,
            updatedAt: Date.now(),
            synced: false,
        };
        store.put(record);
        tx.oncomplete = () => resolve();
        tx.onerror = () => reject(tx.error);
    });
}

export async function markSynced(variantId: string): Promise<void> {
    const db = await openDb();
    return new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, "readwrite");
        const store = tx.objectStore(STORE_NAME);
        const getReq = store.get(variantId);
        getReq.onsuccess = () => {
            const record = getReq.result as LocalProgressRecord | undefined;
            if (record) {
                record.synced = true;
                store.put(record);
            }
        };
        tx.oncomplete = () => resolve();
        tx.onerror = () => reject(tx.error);
    });
}

export async function getLocalProgress(variantId: string): Promise<LocalProgressRecord | null> {
    const db = await openDb();
    return new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, "readonly");
        const store = tx.objectStore(STORE_NAME);
        const req = store.get(variantId);
        req.onsuccess = () => resolve((req.result as LocalProgressRecord | undefined) ?? null);
        req.onerror = () => reject(req.error);
    });
}

export async function getPendingProgress(): Promise<LocalProgressRecord[]> {
    const db = await openDb();
    return new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, "readonly");
        const store = tx.objectStore(STORE_NAME);
        const req = store.getAll();
        req.onsuccess = () => {
            const all = (req.result as LocalProgressRecord[]) ?? [];
            resolve(all.filter((r) => !r.synced));
        };
        req.onerror = () => reject(req.error);
    });
}