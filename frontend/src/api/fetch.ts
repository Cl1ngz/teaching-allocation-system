const apiBase = new URL(API_BASE_URL);

/**
 * A simple wrapper to fetch data from the API.
 * The typesafe API wrapper functions use this to get their data.
 */
export function apiFetch(path: string, init?: RequestInit): Promise<Response> {
    const url = new URL(
        apiBase.href + (path.startsWith("/") ? "" : "/") + path,
        window.location.href,
    );
    return fetch(url, init);
}
