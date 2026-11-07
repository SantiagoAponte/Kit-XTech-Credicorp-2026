import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import boundaries from 'eslint-plugin-boundaries';

// Reglas de dependencia entre capas (A3 §2/§7, ADR-003/008):
//   domain         -> (nada)
//   application    -> domain
//   infrastructure -> application, domain
//   presentation   -> application, domain      (NUNCA infrastructure)
//   app (bootstrap, remote-entry, main, app) -> todas (composition root)
//   ningún import entre micro-frontends
const from = (type, ...to) => ({
  from: { element: { type } },
  allow: { to: { element: { types: [type, ...to] } } },
});

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'src/infrastructure/api/schema.d.ts'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: { globals: { ...globals.browser, ...globals.node } },
    plugins: { boundaries },
    settings: {
      'import/resolver': { node: { extensions: ['.ts', '.tsx', '.js', '.mjs'] } },
      'boundaries/elements': [
        { type: 'domain', pattern: 'src/domain' },
        { type: 'application', pattern: 'src/application' },
        { type: 'infrastructure', pattern: 'src/infrastructure' },
        { type: 'presentation', pattern: 'src/presentation' },
        { type: 'app', pattern: ['src/bootstrap', 'src/*.{ts,tsx}'], partialMatch: false },
        { type: 'tests', pattern: 'tests' },
      ],
    },
    rules: {
      'boundaries/dependencies': [
        'error',
        {
          default: 'disallow',
          policies: [
            from('domain'),
            from('application', 'domain'),
            from('infrastructure', 'application', 'domain'),
            from('presentation', 'application', 'domain'),
            from('app', 'domain', 'application', 'infrastructure', 'presentation'),
            from('tests', 'domain', 'application', 'infrastructure', 'presentation', 'app'),
          ],
        },
      ],
      // Ningún import entre micro-frontends (ni hacia el shell).
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            { group: ['**/shell/**', '**/shell', '**/frontend-*/**', '@mfe/*'], message: 'Prohibido importar código de otro micro-frontend o del shell (ADR-003).' },
          ],
        },
      ],
      // El cliente HTTP vive solo en infrastructure.
      'no-restricted-globals': ['error', { name: 'fetch', message: 'Use un caso de uso; fetch solo en infrastructure.' }],
    },
  },
  {
    files: ['src/infrastructure/**/*.ts', 'tests/**/*.{ts,tsx}', 'vite.config.ts'],
    rules: { 'no-restricted-globals': 'off' },
  },
  {
    files: ['scripts/**/*.mjs', '*.config.js'],
    languageOptions: { globals: globals.node },
  },
);
