package com.bookstore.content;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ffmpeg")
public record FfmpegProperties(
        @DefaultValue("ffmpeg") String path,
        @DefaultValue("10") int segmentDurationSeconds,
        @DefaultValue("128k") String audioBitrate
) {
}