/*
 * Simple verification script for the notification service.
 * Usage (Node 18+):
 *   node --loader ts-node/esm scripts/verify.ts
 * Or compile with tsc and run the output.
 */

type CreateResponse = { id: string; status: string; accepted_at: string };

type StatusResponse = {
  id: string;
  status: string;
  attempts: number;
  nextRetryAt?: string | null;
  lastError?: string | null;
};

const baseUrl = process.env.RC_BASE_URL ?? "http://localhost:8080";
const targetUrl = process.env.RC_TARGET_URL ?? "http://localhost:9000/notify";

async function createNotification(): Promise<CreateResponse> {
  const res = await fetch(`${baseUrl}/notifications`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      targetUrl: targetUrl,
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ping: "pong" })
    })
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`create failed: ${res.status} ${text}`);
  }
  return (await res.json()) as CreateResponse;
}

async function getStatus(id: string): Promise<StatusResponse> {
  const res = await fetch(`${baseUrl}/notifications/${id}`);
  if (!res.ok) {
    throw new Error(`status failed: ${res.status}`);
  }
  return (await res.json()) as StatusResponse;
}

async function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function main() {
  console.log(`baseUrl=${baseUrl}`);
  console.log(`targetUrl=${targetUrl}`);

  const created = await createNotification();
  console.log("created:", created);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
