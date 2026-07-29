import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import Hls from "hls.js";
import { getAccessUrl, getLibrary, updateProgress } from "./libraryApi";
import type { LibraryItemResponse } from "./libraryTypes";
import { getErrorMessage, getStoredToken, API_BASE_URL } from "../../lib/apiClient";
import "./library.css";

const SAVE_DEBOUNCE_MS = 3000;
const SPEED_OPTIONS = [0.75, 1, 1.25, 1.5, 2];

export function AudioPlayerPage() {
    const { variantId } = useParams<{ variantId: string }>();
    const navigate = useNavigate();
    const audioRef = useRef<HTMLAudioElement>(null);
    const hlsRef = useRef<Hls | null>(null);
    const saveTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const [item, setItem] = useState<LibraryItemResponse | null>(null);
    const [audioUrl, setAudioUrl] = useState<string | null>(null);
    const [isStreaming, setIsStreaming] = useState(false);
    const [speed, setSpeed] = useState(1);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!variantId) return;
        let cancelled = false;

        Promise.all([getAccessUrl(variantId), getLibrary()])
            .then(([access, library]) => {
                if (cancelled) return;

                if (access.hlsReady && Hls.isSupported()) {
                    setAudioUrl(`${API_BASE_URL}/api/content/variants/${variantId}/hls/playlist.m3u8`);
                    setIsStreaming(true);
                } else {
                    setAudioUrl(access.accessUrl);
                    setIsStreaming(false);
                }

                const found = library.find((i) => i.productVariantId === variantId) ?? null;
                setItem(found);
                if (found?.playbackSpeed) setSpeed(found.playbackSpeed);
            })
            .catch((err) => {
                if (!cancelled) setError(getErrorMessage(err, "Không tải được nội dung audiobook"));
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [variantId]);

    useEffect(() => {
        if (!audioUrl || !audioRef.current) return;

        if (isStreaming) {
            const token = getStoredToken();
            const hls = new Hls({
                xhrSetup: (xhr) => {
                    if (token) {
                        xhr.setRequestHeader("Authorization", `Bearer ${token}`);
                    }
                },
            });
            hlsRef.current = hls;
            hls.loadSource(audioUrl);
            hls.attachMedia(audioRef.current);

            return () => {
                hls.destroy();
                hlsRef.current = null;
            };
        }

        audioRef.current.src = audioUrl;
        return undefined;
    }, [audioUrl, isStreaming]);

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
            <p className="player-page__author">
                Đang nghe {isStreaming && <span className="player-page__badge">· Streaming HLS</span>}
            </p>

            <audio
                ref={audioRef}
                controls
                onTimeUpdate={handleTimeUpdate}
                onLoadedMetadata={() => {
                    if (audioRef.current) audioRef.current.playbackRate = speed;
                }}
            />

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