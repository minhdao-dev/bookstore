/**
 * Real-time module — WebSocket (STOMP) infrastructure for pushing live
 * updates (e.g. reading/listening progress) to a user's other open
 * sessions/devices. JWT-authenticated at the STOMP CONNECT frame level,
 * separate from the HTTP filter chain.
 */
@NullMarked
package com.bookstore.realtime;

import org.jspecify.annotations.NullMarked;