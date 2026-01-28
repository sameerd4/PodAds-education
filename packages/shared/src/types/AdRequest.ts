export type SlotType = 'pre-roll' | 'mid-roll' | 'post-roll';

export type PodcastCategory = 
  | 'fitness' 
  | 'tech' 
  | 'finance' 
  | 'true-crime' 
  | 'sports' 
  | 'comedy' 
  | 'news' 
  | 'education';

export interface ListenerContext {
  geo: string; // ISO country code, e.g., "US", "GB"
  device: 'mobile' | 'desktop' | 'smart-speaker' | 'car';
  tier: 'free' | 'premium';
  consent: boolean;
  timeOfDay: 'morning' | 'afternoon' | 'evening' | 'night';
}

export interface AdRequest {
  requestId: string;
  podcast: {
    category: PodcastCategory;
    show: string;
    episode: string;
  };
  slot: {
    type: SlotType;
    cuePoint?: number; // seconds into episode (for mid-roll)
  };
  listener: ListenerContext;
  timestamp: string; // ISO 8601
}


