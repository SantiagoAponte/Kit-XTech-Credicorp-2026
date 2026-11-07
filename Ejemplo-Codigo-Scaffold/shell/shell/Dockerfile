# Etapa 1: construcción
FROM node:24-alpine AS build
WORKDIR /app
# URL del remoteEntry que resolverá el NAVEGADOR (se fija en build).
ARG VITE_REMOTE_EXAMPLE_URL=http://localhost:5174/remoteEntry.js
ENV VITE_REMOTE_EXAMPLE_URL=${VITE_REMOTE_EXAMPLE_URL}
COPY package.json package-lock.json .npmrc ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: nginx sin privilegios en el puerto 5173; la plantilla se procesa con envsubst al arrancar.
FROM nginxinc/nginx-unprivileged:alpine
ENV BACKEND_URL=http://backend:8080
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 5173
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1:5173/ >/dev/null || exit 1
