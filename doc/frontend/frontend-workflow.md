# Frontend Workflow

Frontend work follows a PRD-to-visual-reference workflow.

## Required Flow

1. Write or reference the backend PRD.
2. Derive frontend page requirements from the PRD.
3. Generate frontend visual prompts from the page requirements.
4. Use gpt-image-2 to generate reference images for important page states.
5. Select reference images and feed them back into the implementation prompt.
6. Generate or implement frontend code from the page spec, visual references, and API contracts.

## Hard Rules

- Do not freely design core business pages before a backend PRD exists.
- Do not let visual images replace requirements.
- Do not implement from an image alone.
- Every implemented page must trace to a page spec and API contract.
- If a page does not need visual references, document why in the page spec.

## What Backend PRDs Provide

Backend PRDs provide:

- User actions.
- Permissions.
- State machine.
- Data fields.
- API behavior.
- Error states.
- Audit and moderation requirements.

Backend PRDs do not provide:

- Final visual style.
- Component layout.
- Typography choices.
- Image prompts.

## What Frontend Specs Provide

Frontend specs provide:

- Page goal.
- User flows.
- API dependencies.
- UI states.
- Desktop and mobile behavior.
- Accessibility requirements.
- Visual prompt requirements.

## Image Generation

Use gpt-image-2 for important frontend reference images when visual quality matters.

Generate enough states to cover:

- Default content.
- Empty state.
- Loading state.
- Error state.
- No-permission state.
- Review or warning state.
- Mobile layout.
- Dialogs or drawers when relevant.
