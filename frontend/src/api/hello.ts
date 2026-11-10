import { apiFetch } from "./fetch";

export interface HelloResponse {
    message: string;
}

export async function hello(): Promise<HelloResponse> {
    return (await apiFetch("/hello").then((res) => res.json())) as HelloResponse;
}
