/**
 * Seeded random number generator for deterministic simulations
 */
export class SeededRandom {
  private seed: number;

  constructor(seed: number) {
    this.seed = seed;
  }

  /**
   * Generate a random number between 0 and 1
   */
  random(): number {
    this.seed = (this.seed * 9301 + 49297) % 233280;
    return this.seed / 233280;
  }

  /**
   * Generate a random integer between min (inclusive) and max (exclusive)
   */
  randomInt(min: number, max: number): number {
    return Math.floor(this.random() * (max - min)) + min;
  }

  /**
   * Generate a random float between min (inclusive) and max (exclusive)
   */
  randomFloat(min: number, max: number): number {
    return this.random() * (max - min) + min;
  }

  /**
   * Pick a random element from an array
   */
  pick<T>(array: T[]): T {
    return array[this.randomInt(0, array.length)];
  }

  /**
   * Shuffle an array (mutates the array)
   */
  shuffle<T>(array: T[]): T[] {
    for (let i = array.length - 1; i > 0; i--) {
      const j = this.randomInt(0, i + 1);
      [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
  }
}


