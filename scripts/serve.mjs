// Servidor estático simples para desenvolvimento local do Mira.
// Uso: npm run serve  (ou via preview_start)
import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { extname, join, normalize } from "node:path";

const ROOT = process.cwd();
const PORT = process.env.PORT || 5050;
const TYPES = {
  ".html": "text/html",
  ".css": "text/css",
  ".js": "text/javascript",
  ".mjs": "text/javascript",
  ".json": "application/json",
  ".webmanifest": "application/manifest+json",
  ".png": "image/png",
  ".svg": "image/svg+xml",
};

createServer(async (req, res) => {
  let p = decodeURIComponent(req.url.split("?")[0]);
  if (p === "/") p = "/index.html";
  const fp = normalize(join(ROOT, p));
  if (!fp.startsWith(ROOT)) {
    res.writeHead(403);
    return res.end("403");
  }
  try {
    const data = await readFile(fp);
    res.writeHead(200, {
      "Content-Type": TYPES[extname(fp)] || "application/octet-stream",
    });
    res.end(data);
  } catch {
    res.writeHead(404);
    res.end("404");
  }
}).listen(PORT, () => console.log(`Mira em http://localhost:${PORT}`));
