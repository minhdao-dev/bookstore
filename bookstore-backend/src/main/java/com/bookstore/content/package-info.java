/**
 * Content module (digital-only) — DRM and file delivery for ebook/audiobook:
 * encrypted originals, chunked HLS/DASH streaming for audiobooks, per-device
 * licensing, and download/listen limits. Heaviest I/O load in the system —
 * first candidate to split into its own microservice.
 */
package com.bookstore.content;
