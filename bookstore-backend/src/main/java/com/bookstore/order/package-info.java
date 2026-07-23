/**
 * Order/Cart module — mixed cart (ebook + physical book in the same
 * order), checkout/payment, and subscription billing for digital
 * products. Fulfillment status is tracked per line-item, not per order,
 * since ebooks fulfill instantly while physical items ship separately.
 */
package com.bookstore.order;
