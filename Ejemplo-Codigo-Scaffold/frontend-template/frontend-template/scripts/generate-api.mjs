// Genera src/infrastructure/api/schema.d.ts desde el contrato OpenAPI.
// Ruta configurable: OPENAPI_SPEC=ruta/al/contrato.yaml npm run generate:api
import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
// Espacio de trabajo del equipo: ../contracts/ ; dentro del kit: ../contracts-template/
const candidates = ['../contracts/example.openapi.yaml', '../contracts-template/example.openapi.yaml'];
const spec = process.env.OPENAPI_SPEC ?? candidates.find((c) => existsSync(c)) ?? candidates[0];
const out = 'src/infrastructure/api/schema.d.ts';
console.log(`openapi-typescript: ${spec} -> ${out}`);
const r = spawnSync('npx', ['openapi-typescript', spec, '-o', out], { stdio: 'inherit' });
process.exit(r.status ?? 1);
