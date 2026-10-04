// Tests must never call the real Anthropic API, even if the developer has a key exported.
delete process.env.ANTHROPIC_API_KEY;
process.env.AI_FALLBACK_CHUNK_MS = '10';
