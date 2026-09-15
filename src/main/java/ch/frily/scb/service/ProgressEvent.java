package ch.frily.scb.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * A progress-record that is sent to the frontend via sse as a stream which the frontend can continuously read while making a fetch
 */
public record ProgressEvent(String step, int current, int total, String message) {
}