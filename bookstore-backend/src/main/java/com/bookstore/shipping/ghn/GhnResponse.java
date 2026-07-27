package com.bookstore.shipping.ghn;

public record GhnResponse<T>(int code, String message, T data) {
}