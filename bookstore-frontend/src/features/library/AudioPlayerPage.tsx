import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { getAccessUrl, getLibrary, updateProgress } from "./libraryApi";
import type { LibraryItemResponse } from "./libraryTypes";
import "./library.css";

const SAVE_DEBOUNCE_MS = 3000;
const SPEED_OPTIONS = [0.75, 1, 1.25, 1.5, 2];

export function AudioPlayerPage() {
    const { variantId } = useParams<{ variantId: string }>();
    const navigate = useNavigate();
    const audioRef = useRef<HTMLAudioElement>(null);
    const saveTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [item, setItem] = useState<LibraryItemResponse | null>(null);
    const [audioUrl, setAudioUrl] = useState<string | null>(null);
    const [speed, setSpeed] = useState(1);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!variantId) return;
        let cancelled = false;

        Promise.all([getAccessUrl(variantId), getLibrary()])
            .then(([access, library]) => {
                if (cancelled) return;
                setAudioUrl(access.accessUrl);
                const found = library.find((i) => i.productVariantId === variantId) ?? null;
                setItem(found);
                if (found?.playbackSpeed) setSpeed(found.playbackSpeed);
            })
            .catch(() => {
                if (!cancelled) setError("Không tải được nội dung audiobook");
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [variantId]);

    useEffect(() => {
        if (audioRef.current && item?.position) {
            const seconds = Number(item.position);
            if (!Number.isNaN(seconds)) {
                audioRef.current.currentTime = seconds;
            }
        }
    }, [item, audioUrl]);

    function scheduleSave(currentTimeSeconds: number, playbackSpeed: number) {
        if (!variantId) return;
        if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
        saveTimeoutRef.current = setTimeout(() => {
            updateProgress(variantId, {
                position: String(Math.floor(currentTimeSeconds)),
                playbackSpeed,
            }).catch(() => {
                // Lưu tiến độ thất bại âm thầm
            });
        }, SAVE_DEBOUNCE_MS);
    }

    function handleTimeUpdate() {
        if (audioRef.current) {
            scheduleSave(audioRef.current.currentTime, speed);
        }
    }

    function handleSpeedChange(newSpeed: number) {
        setSpeed(newSpeed);
        if (audioRef.current) {
            audioRef.current.playbackRate = newSpeed;
            scheduleSave(audioRef.current.currentTime, newSpeed);
        }
    }

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;
    if (error) return <p className="catalog-state">{error}</p>;

    return (
        <div className="player-page">
            <h1>{item?.bookTitle ?? "Audiobook"}</h1>
            <p className="player-page__author">Đang nghe</p>

            {audioUrl && (
                <audio
                    ref={audioRef}
                    src={audioUrl}
                    controls
                    onTimeUpdate={handleTimeUpdate}
                    onLoadedMetadata={() => {
                        if (audioRef.current) audioRef.current.playbackRate = speed;
                    }}
                />
            )}

            <div className="player-speed">
                <label htmlFor="speed">Tốc độ phát</label>
                <select
                    id="speed"
                    value={speed}
                    onChange={(e) => handleSpeedChange(Number(e.target.value))}
                >
                    {SPEED_OPTIONS.map((option) => (
                        <option key={option} value={option}>
                            {option}x
                        </option>
                    ))}
                </select>
            </div>

            <div style={{ marginTop: "2rem" }}>
                <button
                    type="button"
                    className="admin-btn admin-btn--ghost"
                    onClick={() => navigate("/library")}
                >
                    Quay lại tủ sách
                </button>
            </div>
        </div>
    );
}