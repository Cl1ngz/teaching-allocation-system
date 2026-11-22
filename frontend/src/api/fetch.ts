import { ApiError, FetchError, ResponseSyntaxError } from "./error";

const apiBase = new URL(API_BASE_URL).href.replace(/\/$/, "");

/**
 * A simple wrapper to fetch data from the API.
 * This is appropriate if the data isn't JSON;
 * normally apiFetchJson should be used instead.
 */
export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
    const url = new URL(apiBase + (path.startsWith("/") ? "" : "/") + path, window.location.origin);
    try {
        const res = await fetch(url, init);
        if (res.ok) {
            return res;
        } else {
            throw new ApiError(await res.json());
        }
    } catch (err) {
        if (err instanceof ApiError) {
            throw err;
        } else if (err instanceof SyntaxError) {
            throw new ResponseSyntaxError(err);
        } else {
            throw new FetchError(err as Error);
        }
    }
}

export async function responseToJson<Result>(response: Response): Promise<Result> {
    try {
        return await response.json();
    } catch (err) {
        throw new ResponseSyntaxError(err as SyntaxError);
    }
}
