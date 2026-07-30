package com.bookstore.content.service;

import com.bookstore.content.FfmpegProperties;
import com.bookstore.content.MinioProperties;
import com.bookstore.content.exception.ContentStorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HlsTranscodeService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FfmpegProperties ffmpegProperties;

    public void transcodeToHls(UUID variantId, File sourceAudioFile) {
        Path outputDir;
        try {
            outputDir = Files.createTempDirectory("hls-" + variantId);
        } catch (IOException ex) {
            throw new ContentStorageException("Failed to create temp directory for HLS transcode", ex);
        }

        try {
            runFfmpeg(sourceAudioFile, outputDir);
            uploadOutputFiles(variantId, outputDir);
        } finally {
            deleteDirectoryQuietly(outputDir);
        }
    }

    private List<String> buildFfmpegCommand(File sourceAudioFile, Path outputDir) {
        Path playlistPath = outputDir.resolve("playlist.m3u8");
        Path segmentPattern = outputDir.resolve("segment%03d.ts");

        return List.of(
                ffmpegProperties.path(),
                "-y",
                "-i", sourceAudioFile.getAbsolutePath(),
                "-vn",
                "-c:a", "aac",
                "-b:a", ffmpegProperties.audioBitrate(),
                "-hls_time", String.valueOf(ffmpegProperties.segmentDurationSeconds()),
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", segmentPattern.toString(),
                playlistPath.toString()
        );
    }

    private void runFfmpeg(File sourceAudioFile, Path outputDir) {
        List<String> command = buildFfmpegCommand(sourceAudioFile, outputDir);

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            String ffmpegOutput = new String(process.getInputStream().readAllBytes());
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                throw new ContentStorageException("FFmpeg transcode timed out");
            }
            if (process.exitValue() != 0) {
                log.warn("FFmpeg failed with exit code {}: {}", process.exitValue(), ffmpegOutput);
                throw new ContentStorageException("FFmpeg transcode failed with exit code " + process.exitValue());
            }
        } catch (IOException ex) {
            throw new ContentStorageException("Failed to start FFmpeg process", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ContentStorageException("FFmpeg transcode was interrupted", ex);
        }
    }

    private void uploadOutputFiles(UUID variantId, Path outputDir) {
        try (Stream<Path> files = Files.list(outputDir)) {
            for (Path file : files.toList()) {
                String fileName = file.getFileName().toString();
                String storageKey = "hls/" + variantId + "/" + fileName;
                String contentType = fileName.endsWith(".m3u8")
                        ? "application/vnd.apple.mpegurl"
                        : "video/mp2t";

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.bucket())
                        .object(storageKey)
                        .stream(Files.newInputStream(file), Files.size(file), -1L)
                        .contentType(contentType)
                        .build());
            }
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to upload HLS output to storage", ex);
        }
    }

    private void deleteDirectoryQuietly(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            log.warn("Failed to delete temp file {}: {}", path, ex.getMessage());
                        }
                    });
        } catch (IOException ex) {
            log.warn("Failed to clean up temp directory {}: {}", dir, ex.getMessage());
        }
    }
}