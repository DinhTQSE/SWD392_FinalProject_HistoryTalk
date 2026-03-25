# Historical Context Publish API Plan

## Goal
- Add a publish endpoint for historical contexts (flip `isDraft` to false) and set default `isDraft=false` when creating contexts.

## Scope
- Controller: new publish endpoint under `HistoricalContextController`.
- Service: add `publishContext` to interface + implementation.
- DTO/Entity: default `isDraft` to false on create; guard against null.
- (Optional) DB: migration to set column default `is_draft=false` for future rows.

## Steps
1) **Controller**
   - Add `PATCH /api/v1/historical-contexts/{contextId}/publish` with `@PreAuthorize(ROLE_STAFF|ROLE_ADMIN)`, returns `ApiResponse<?>`.
   - Inject `SecurityUtils` to get userId/role; call service.publishContext.

2) **Service Interface**
   - Add method signature: `void publishContext(String contextId, String userId, String userRole);`.

3) **Service Impl**
   - Load context by id; if `deletedAt != null` → `ResourceNotFoundException` (keep 404 leak prevention).
   - Require staff/admin (reuse `isStaffOrAdmin`); else `InvalidRequestException`.
   - If already `isDraft == false`, return silently (idempotent).
   - Else set `isDraft=false`, `updatedDate=now`, save.

4) **Defaults on Create**
   - In `CreateHistoricalContextRequest`, set `isDraft` default to `false`.
   - In entity builder default, set `isDraft=false`; optionally add `@PrePersist` to force false when null.
   - In `HistoricalContextServiceImpl.createContext`, coalesce null to false before building.


## Testing (manual for now)
- Create context (no isDraft field) ⇒ stored with `is_draft=false`.
- Create context with `isDraft=true` ⇒ stays draft.
- Publish draft context ⇒ returns 200, `isDraft=false`.
- Publish already published ⇒ idempotent 200.
- Publish soft-deleted ⇒ 404.
- Customer role calling publish ⇒ 400 InvalidRequest.
