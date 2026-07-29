import { useEffect, useState } from "react";
import { Link } from "react-router";
import { useAuth } from "../auth/AuthContext";
import { getRecommendations } from "./recommendationApi";
import type { RecommendationResponse } from "./recommendationTypes";
import { Skeleton } from "../../components/Skeleton";
import "./recommendations.css";

export function RecommendationSection() {
    const { user } = useAuth();
    const [recommendations, setRecommendations] = useState<RecommendationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!user) {
            setIsLoading(false);
            return;
        }
        let cancelled = false;
        setIsLoading(true);

        getRecommendations()
            .then((result) => {
                if (!cancelled) setRecommendations(result);
            })
            .catch(() => {
                if (!cancelled) setRecommendations([]);
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [user]);

    if (!user) return null;
    if (!isLoading && recommendations.length === 0) return null;

    return (
        <section className="recommendation-section">
            <h2>Gợi ý cho bạn</h2>

            {isLoading && (
                <div className="recommendation-grid">
                    <Skeleton height="140px" />
                    <Skeleton height="140px" />
                    <Skeleton height="140px" />
                </div>
            )}

            {!isLoading && (
                <div className="recommendation-grid">
                    {recommendations.map((rec) => (
                        <Link key={rec.bookId} to={`/catalog/${rec.bookId}`} className="recommendation-card">
                            <div className="recommendation-card__genre">{rec.genre}</div>
                            <h3>{rec.title}</h3>
                            <div className="recommendation-card__author">{rec.author}</div>
                            {rec.reviewCount > 0 && (
                                <div className="recommendation-card__rating">
                                    ★ {rec.averageRating.toFixed(1)} ({rec.reviewCount})
                                </div>
                            )}
                        </Link>
                    ))}
                </div>
            )}
        </section>
    );
}