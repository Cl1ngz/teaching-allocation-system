import { type EndpointErrorType } from "./error";
import { apiFetch, responseToJson } from "./fetch";

export interface HelloResponse {
    message: string;
}

export type HelloError = EndpointErrorType<never>;

export async function hello(): Promise<HelloResponse> {
    return apiFetch("/v1/hello").then(responseToJson<HelloResponse>);
}
