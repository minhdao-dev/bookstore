/**
 * Entitlement module (digital-only) — grants content access (purchase,
 * rental, subscription) right after successful payment. Deliberately kept
 * as a table/domain separate from Order, to support multiple ownership
 * models cleanly from day one.
 */
package com.bookstore.entitlement;
