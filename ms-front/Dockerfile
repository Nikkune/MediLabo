# syntax=docker/dockerfile:1

# Build stage
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# Serve stage
FROM nginx:1.27-alpine
# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*
# Copy build output
COPY --from=builder /app/dist /usr/share/nginx/html
# Optional: custom nginx config for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx","-g","daemon off;"]
