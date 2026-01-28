export type FilterReasonCode =
  | 'campaign_inactive'
  | 'campaign_ended'
  | 'outside_schedule_window'
  | 'geo_mismatch'
  | 'device_mismatch'
  | 'tier_mismatch'
  | 'category_mismatch'
  | 'show_mismatch'
  | 'excluded_category'
  | 'duration_too_long'
  | 'duration_too_short'
  | 'frequency_cap_exceeded'
  | 'budget_exhausted'
  | 'pacing_limit_exceeded'
  | 'brand_safety_violation'
  | 'creative_not_approved'
  | 'slot_type_mismatch';

export interface FilterResult {
  passed: boolean;
  reasonCode?: FilterReasonCode;
  details?: string; // Human-readable explanation
  metadata?: Record<string, unknown>; // Additional context
}


