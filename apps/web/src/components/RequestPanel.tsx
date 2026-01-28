import { useState, useEffect } from 'react';
import { AdRequest, PodcastCategory, SlotType } from '@podads/shared';

interface RequestPanelProps {
  onRunOnce: (request: AdRequest) => void;
  onRunBatch: (request: AdRequest) => void;
  isLoading: boolean;
  initialCategory?: PodcastCategory;
  initialShow?: string;
  initialEpisode?: string;
}

export default function RequestPanel({ onRunOnce, onRunBatch, isLoading, initialCategory, initialShow, initialEpisode }: RequestPanelProps) {
  const [category, setCategory] = useState<PodcastCategory>(initialCategory || 'fitness');
  const [show, setShow] = useState(initialShow || 'The Daily Run');
  const [episode, setEpisode] = useState(initialEpisode || 'Episode 42');
  
  // Update fields when initial props change (from URL params)
  useEffect(() => {
    if (initialCategory) {
      setCategory(initialCategory);
    }
    if (initialShow) {
      setShow(initialShow);
    }
    if (initialEpisode) {
      setEpisode(initialEpisode);
    }
  }, [initialCategory, initialShow, initialEpisode]);
  const [slotType, setSlotType] = useState<SlotType>('mid-roll');
  const [cuePoint, setCuePoint] = useState(300);
  const [geo, setGeo] = useState('US');
  const [device, setDevice] = useState<'mobile' | 'desktop' | 'smart-speaker' | 'car'>('mobile');
  const [tier, setTier] = useState<'free' | 'premium'>('free');
  const [consent, setConsent] = useState(true);
  const [timeOfDay, setTimeOfDay] = useState<'morning' | 'afternoon' | 'evening' | 'night'>('afternoon');

  const handleRunOnce = () => {
    const request: AdRequest = {
      requestId: `req-${Date.now()}`,
      podcast: {
        category,
        show,
        episode,
      },
      slot: {
        type: slotType,
        cuePoint: slotType === 'mid-roll' ? cuePoint : undefined,
      },
      listener: {
        geo,
        device,
        tier,
        consent,
        timeOfDay,
      },
      timestamp: new Date().toISOString(),
    };
    onRunOnce(request);
  };

  const handleRunBatch = () => {
    const request: AdRequest = {
      requestId: `req-batch-${Date.now()}`,
      podcast: {
        category,
        show,
        episode,
      },
      slot: {
        type: slotType,
        cuePoint: slotType === 'mid-roll' ? cuePoint : undefined,
      },
      listener: {
        geo,
        device,
        tier,
        consent,
        timeOfDay,
      },
      timestamp: new Date().toISOString(),
    };
    onRunBatch(request);
  };

  return (
    <div className="bg-gray-800 rounded-lg p-4 sm:p-6 space-y-4">
      <h2 className="text-lg sm:text-xl font-semibold mb-4">Request Configuration</h2>

      <div>
        <label className="block text-sm font-medium mb-1">Podcast Category</label>
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value as PodcastCategory)}
          className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
        >
          <option value="fitness">Fitness</option>
          <option value="tech">Tech</option>
          <option value="finance">Finance</option>
          <option value="true-crime">True Crime</option>
          <option value="sports">Sports</option>
          <option value="comedy">Comedy</option>
          <option value="news">News</option>
          <option value="education">Education</option>
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">Show</label>
        <input
          type="text"
          value={show}
          onChange={(e) => setShow(e.target.value)}
          className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
        />
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">Episode</label>
        <input
          type="text"
          value={episode}
          onChange={(e) => setEpisode(e.target.value)}
          className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
        />
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">Slot Type</label>
        <select
          value={slotType}
          onChange={(e) => setSlotType(e.target.value as SlotType)}
          className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
        >
          <option value="pre-roll">Pre-roll</option>
          <option value="mid-roll">Mid-roll</option>
          <option value="post-roll">Post-roll</option>
        </select>
      </div>

      {slotType === 'mid-roll' && (
        <div>
          <label className="block text-sm font-medium mb-1">Cue Point (seconds)</label>
          <input
            type="number"
            value={cuePoint}
            onChange={(e) => setCuePoint(Number(e.target.value))}
            className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
          />
        </div>
      )}

      <div className="pt-4 border-t border-gray-700">
        <h3 className="text-sm font-semibold mb-2">Listener Context</h3>
        
        <div className="space-y-3">
          <div>
            <label className="block text-xs mb-1">Geo</label>
            <input
              type="text"
              value={geo}
              onChange={(e) => setGeo(e.target.value)}
              className="w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm"
              placeholder="US"
            />
          </div>

          <div>
            <label className="block text-xs mb-1">Device</label>
            <select
              value={device}
              onChange={(e) => setDevice(e.target.value as any)}
              className="w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm"
            >
              <option value="mobile">Mobile</option>
              <option value="desktop">Desktop</option>
              <option value="smart-speaker">Smart Speaker</option>
              <option value="car">Car</option>
            </select>
          </div>

          <div>
            <label className="block text-xs mb-1">Tier</label>
            <select
              value={tier}
              onChange={(e) => setTier(e.target.value as any)}
              className="w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm"
            >
              <option value="free">Free</option>
              <option value="premium">Premium</option>
            </select>
          </div>

          <div>
            <label className="block text-xs mb-1">Time of Day</label>
            <select
              value={timeOfDay}
              onChange={(e) => setTimeOfDay(e.target.value as any)}
              className="w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm"
            >
              <option value="morning">Morning</option>
              <option value="afternoon">Afternoon</option>
              <option value="evening">Evening</option>
              <option value="night">Night</option>
            </select>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="consent"
              checked={consent}
              onChange={(e) => setConsent(e.target.checked)}
              className="w-4 h-4"
            />
            <label htmlFor="consent" className="text-xs">Consent</label>
          </div>
        </div>
      </div>

      <div className="pt-4 space-y-2">
        <button
          onClick={handleRunOnce}
          disabled={isLoading}
          className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed rounded transition-colors"
        >
          {isLoading ? 'Running...' : 'Run Once'}
        </button>
        <button
          onClick={handleRunBatch}
          disabled={isLoading}
          className="w-full px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed rounded transition-colors"
        >
          {isLoading ? 'Running...' : 'Run 100x'}
        </button>
      </div>
    </div>
  );
}


