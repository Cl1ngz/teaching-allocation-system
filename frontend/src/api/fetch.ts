const apiBase = new URL(API_BASE_URL).href.replace(/\/$/, "");

/**
 * A simple wrapper to fetch data from the API.
 * The typesafe API wrapper functions use this to get their data.
 */
export function apiFetch(path: string, init?: RequestInit): Promise<Response> {
    const url = new URL(apiBase + (path.startsWith("/") ? "" : "/") + path, window.location.origin);
    return fetch(url, init);
}
