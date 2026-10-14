import { createReadStream, existsSync, statSync } from "node:fs";
import { createServer } from "node:http";
import { extname, join, normalize, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const buildDirectory = fileURLToPath(new URL("./dist", import.meta.url));
const contentTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".webp": "image/webp",
};

createServer((request, response) => {
  if (request.method !== "GET" && request.method !== "HEAD") {
    response.writeHead(405, { Allow: "GET, HEAD" }).end();
    return;
  }

  let pathname;
  try {
    pathname = decodeURIComponent(new URL(request.url, "http://localhost").pathname);
  } catch {
    response.writeHead(400).end();
    return;
  }

  const requestedFile = resolve(buildDirectory, `.${normalize(pathname)}`);
  if (requestedFile !== buildDirectory && !requestedFile.startsWith(`${buildDirectory}${sep}`)) {
    response.writeHead(403).end();
    return;
  }

  const file = existsSync(requestedFile) && statSync(requestedFile).isFile()
    ? requestedFile
    : join(buildDirectory, "index.html");
  response.writeHead(200, {
    "Content-Type": contentTypes[extname(file)] || "application/octet-stream",
    "X-Content-Type-Options": "nosniff",
  });
  if (request.method === "HEAD") {
    response.end();
    return;
  }
  createReadStream(file).pipe(response);
}).listen(Number(process.env.PORT) || 4173, "0.0.0.0");
