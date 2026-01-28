import { create } from 'zustand';
import { AdDecision, AdRequest } from '@podads/shared';

export interface BatchResults {
  totalRequests: number;
  fills: number;
  noFills: number;
  totalRevenue: number; // in dollars
  averageCpm: number; // in dollars
  fillRate: number; // percentage
}

interface DecisionState {
  currentRequest: AdRequest | null;
  currentDecision: AdDecision | null;
  isLoading: boolean;
  batchResults: BatchResults | null;
  setRequest: (request: AdRequest) => void;
  setDecision: (decision: AdDecision) => void;
  setLoading: (loading: boolean) => void;
  setBatchResults: (results: BatchResults | null) => void;
  clear: () => void;
}

export const useDecisionStore = create<DecisionState>((set) => ({
  currentRequest: null,
  currentDecision: null,
  isLoading: false,
  batchResults: null,
  setRequest: (request) => set({ currentRequest: request }),
  setDecision: (decision) => set({ currentDecision: decision }), // Don't clear batchResults here
  setLoading: (loading) => set({ isLoading: loading }),
  setBatchResults: (results) => set({ batchResults: results }),
  clear: () => set({ currentRequest: null, currentDecision: null, isLoading: false, batchResults: null }),
}));


