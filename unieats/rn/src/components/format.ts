export function priceLabel(level: number): string {
  return "$".repeat(Math.max(1, Math.min(3, level)));
}

export function starsLabel(stars: number): string {
  return "★".repeat(stars) + "☆".repeat(5 - stars);
}
