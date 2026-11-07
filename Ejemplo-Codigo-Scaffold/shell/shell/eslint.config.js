import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,tsx,js,mjs}'],
    languageOptions: { globals: { ...globals.browser, ...globals.node } },
    rules: {
      // El shell no importa código de micro-frontends: solo los carga en ejecución (remotes/).
      'no-restricted-imports': ['error', { patterns: [{ group: ['**/frontend-template/**', '../frontend-*/**'], message: 'Prohibido importar código de un micro-frontend; se carga por federación.' }] }],
    },
  },
);
