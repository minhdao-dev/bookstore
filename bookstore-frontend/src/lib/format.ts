export function formatPrice(amount: number, currency: string): string {
    const locale = currency === "VND" ? "vi-VN" : "en-US";
    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency,
        maximumFractionDigits: currency === "VND" ? 0 : 2,
    }).format(amount);
}

export function formatDate(isoDate: string): string {
    return new Date(isoDate).toLocaleDateString("vi-VN");
}

export function formatDateTime(isoDate: string): string {
    return new Date(isoDate).toLocaleString("vi-VN");
}