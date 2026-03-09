# Theming Guidelines

Use semantic theme tokens from `src/assets/themes.css` instead of hardcoded colors.

## Rules
- Do not hardcode page/background/text/input colors in component styles.
- Use text tokens:
  - `--theme-text-primary` for core text
  - `--theme-text-secondary` for metadata/subtitles
  - `--theme-text-subtle` for helper/placeholder text
- Use surface tokens:
  - `--theme-surface-elevated` for cards/panels
  - `--theme-surface-1`/`--theme-surface-2` for nested layers
  - `--theme-surface-border` or `--theme-divider` for separators
- Use input tokens:
  - `--theme-input-bg`
  - `--theme-input-border`
  - `--theme-input-text`
  - `--theme-input-placeholder`
- Use CTA tokens:
  - `--theme-button-primary-bg` / `--theme-button-primary-text`
  - `--theme-button-secondary-bg` / `--theme-button-secondary-text`
  - `--theme-button-danger-bg` / `--theme-button-danger-text`
- Use `--theme-focus-ring` for focus states.
- Use theme fonts:
  - body: `--theme-font-body`
  - headings: `--theme-font-heading`

## Contrast
- Secondary text must stay readable on all surfaces.
- Avoid low-opacity text on light surfaces in modern theme.
- Avoid forcing global `!important` color overrides for tags (`p`, `li`, headings).

## Motion and Density
- Respect:
  - `html[data-compact='true']`
  - `html[data-reduced-motion='true']`