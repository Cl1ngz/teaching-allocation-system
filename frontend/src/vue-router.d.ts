import type { BaseError } from "./api/error";

/**
 * Augments vue-router so that data loader errors are typed with the
 * application's error interface. Every error thrown by the API layer
 * (ApiError, FetchError, ResponseSyntaxError) implements it; see
 * src/api/error.ts.
 */
declare module "vue-router" {
    interface TypesConfig {
        Error: BaseError;
    }
}
