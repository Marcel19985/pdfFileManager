# --- Stage 1: React bauen Vite
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Vite: erstellt "dist"
RUN npm run build

# --- Stage 2: NGINX als Webserver + Reverse Proxy
FROM nginx:alpine
# NGINX-Config ins Image
COPY ./nginx/default.conf /etc/nginx/conf.d/default.conf
# Build-Output in Webroot kopieren (passe Ordner an falls CRA)
COPY --from=build /app/dist /usr/share/nginx/html

