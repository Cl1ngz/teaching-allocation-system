import {
    type EndpointErrorType,
    type InternalError,
    type MalformedRequestError,
    type MethodNotAllowedError,
    type NotFoundError,
    type ServiceUnavailableError,
    type ValidationError,
} from "./error";
import { apiFetch, responseToJson } from "./fetch";

export interface TeacherListItem {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    degreeCode: string;
    degreeName: string;
    unitId: number | null;
    unitName: string | null;
    positionName: string | null;
    active: boolean;
}

export interface TeacherPage {
    content: TeacherListItem[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export type TeacherListError = EndpointErrorType<
    | ValidationError
    | MalformedRequestError
    | NotFoundError
    | MethodNotAllowedError
    | ServiceUnavailableError
    | InternalError
>;

export function listTeachers(page = 0, size = 20): Promise<TeacherPage> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    return apiFetch(`/v1/teachers?${params}`).then(responseToJson<TeacherPage>);
}
