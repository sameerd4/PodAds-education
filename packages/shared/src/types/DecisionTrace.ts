import { AdRequest } from './AdRequest';
import { AdDecision } from './AdDecision';

export interface DecisionTrace {
  decisionId: string;
  request: AdRequest;
  decision: AdDecision;
  createdAt: string; // ISO 8601
}


