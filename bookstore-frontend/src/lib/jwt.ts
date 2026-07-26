export interface JwtClaims {
    sub: string;
    email: string;
    role: string;
    exp: number;
    iat: number;
}

export function decodeJwt(token: string): JwtClaims {
    const payload = token.split(".")[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const json = atob(padded);
    return JSON.parse(json) as JwtClaims;
}

export function isTokenExpired(claims: JwtClaims): boolean {
    return claims.exp * 1000 <= Date.now();
}