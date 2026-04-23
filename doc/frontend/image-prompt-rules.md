# Image Prompt Rules

Image prompts are generated from page specs, not from vague ideas.

## Prompt Inputs

Each prompt should use:

- Page spec.
- Target viewport.
- Required states.
- Realistic content examples.
- UI rules.
- Brand and tone constraints.

## Prompt Outputs

Prompts should request reference images for:

- Desktop default state.
- Mobile default state.
- Empty or onboarding state.
- Error or no-permission state.
- Review, warning, or trust-label state when relevant.
- Dialogs, drawers, or editor states when relevant.

## Traceability

Each generated image should be traceable to:

- Backend PRD.
- Page spec.
- Prompt text.
- Target state.

## Use In Implementation

- Images are visual references, not source of truth.
- If image and spec conflict, the spec wins.
- If the image reveals a better interaction, update the page spec before implementing it.
