import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useDecisionStore } from '../stores/useDecisionStore';
import { runDecisionPipeline } from '../lib/simulation/engine';
import { AdRequest, AdDecision, PodcastCategory } from '@podads/shared';
import RequestPanel from '../components/RequestPanel';
import PipelineStepper from '../components/PipelineStepper';
import AuctionBoard from '../components/AuctionBoard';
import MetricsStrip from '../components/MetricsStrip';
import ExplainDrawer from '../components/ExplainDrawer';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

export default function LivePage() {
  const [searchParams] = useSearchParams();
  const { currentDecision, setDecision, setRequest, setLoading, setBatchResults } = useDecisionStore();
  const [showExplain, setShowExplain] = useState(false);
  
  // Read seed from URL params, default to 12345
  const urlSeed = searchParams.get('seed');
  const [seed, setSeed] = useState(urlSeed ? Number(urlSeed) : 12345);
  
  // Read category, show, and episode from URL params
  const urlCategory = searchParams.get('category') as PodcastCategory | null;
  const urlShow = searchParams.get('show');
  const urlEpisode = searchParams.get('episode');
  
  const [useBackend, setUseBackend] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Update seed when URL param changes
  useEffect(() => {
    if (urlSeed) {
      setSeed(Number(urlSeed));
    }
  }, [urlSeed]);

  const handleRunOnce = async (request: AdRequest) => {
    setRequest(request);
    setLoading(true);
    setError(null);
    // Clear batch results for single decision
    setBatchResults(null);
    
    if (useBackend) {
      try {
        const response = await fetch(`${API_BASE_URL}/v1/decision?seed=${seed}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(request),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));
          throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        const decision = await response.json();
        
        // Check if decision is valid (has required fields)
        if (!decision || !decision.decisionId) {
          throw new Error('Invalid response from server');
        }
        
        // No error if all ads filtered out - that's a valid outcome
        // The decision will have winner: null and noFillReason
        setDecision(decision);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch decision';
        
        // Don't show error for network/CORS issues or generic "unknown error" - just fallback silently
        if (errorMessage.includes('Failed to fetch') || 
            errorMessage.includes('NetworkError') || 
            errorMessage.includes('CORS') ||
            errorMessage.includes('Unknown error') ||
            errorMessage === 'Unknown error occurred') {
          // Network/generic error - fallback silently without showing error message
          console.warn('Backend error, falling back to local simulation:', err);
          setError(null); // Don't show error to user
        } else {
          // Specific server error - show it
          setError(errorMessage);
          console.error('API error:', err);
        }
        
        // Fallback to local simulation on error
        const decision = runDecisionPipeline(request, seed);
        setDecision(decision);
      } finally {
        setLoading(false);
      }
    } else {
      // Use local simulation engine
      setTimeout(() => {
        const decision = runDecisionPipeline(request, seed);
        setDecision(decision);
        setLoading(false);
      }, 100);
    }
  };


  const handleRunBatch = async (request: AdRequest, count: number) => {
    setRequest(request);
    setLoading(true);
    setError(null);
    
    let decisions: AdDecision[] = [];
    
    if (useBackend) {
      // Use batch endpoint for much faster processing
      try {
        const response = await fetch(`${API_BASE_URL}/v1/decision/batch?seed=${seed}&count=${count}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(request),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));
          throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        const batchResult = await response.json();
        decisions = batchResult.decisions || [];
      } catch (err) {
        console.error('Batch request failed:', err);
        // Fallback to local simulation on error
        for (let i = 0; i < count; i++) {
          const decision = runDecisionPipeline(request, seed + i);
          decisions.push(decision);
        }
      }
    } else {
      // Run batch simulation locally
      for (let i = 0; i < count; i++) {
        const decision = runDecisionPipeline(request, seed + i);
        decisions.push(decision);
      }
    }
    
    // Calculate batch results
    const fills = decisions.filter(d => d.winner).length;
    const noFills = decisions.length - fills;
    
    // Calculate revenue: CPM = Cost Per Mille (per 1000 impressions)
    // pricePaid is in cents, so revenue per impression = pricePaid / 100000 (pricePaid/100 for CPM, then /1000 for per impression)
    let totalRevenue = 0;
    let totalCpm = 0;
    let cpmCount = 0;
    
    decisions.forEach(decision => {
      if (decision.winner?.serve.pricePaid) {
        const revenuePerImpression = decision.winner.serve.pricePaid / 100000; // Convert cents to dollars per impression
        totalRevenue += revenuePerImpression;
        totalCpm += decision.winner.serve.pricePaid / 100; // Convert cents to dollars for CPM
        cpmCount++;
      }
    });
    
    const averageCpm = cpmCount > 0 ? totalCpm / cpmCount : 0;
    const fillRate = decisions.length > 0 ? (fills / decisions.length) * 100 : 0;
    
    // Store batch results
    setBatchResults({
      totalRequests: decisions.length,
      fills,
      noFills,
      totalRevenue,
      averageCpm,
      fillRate,
    });
    
    // Show the last decision
    if (decisions.length > 0) {
      setDecision(decisions[decisions.length - 1]);
    }
    setLoading(false);
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <h1 className="text-2xl sm:text-3xl font-bold">Live Decision Simulator</h1>
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 sm:gap-4">
          <label className="text-sm flex items-center gap-2">
            <input
              type="checkbox"
              checked={useBackend}
              onChange={(e) => setUseBackend(e.target.checked)}
              className="w-4 h-4"
            />
            <span className="whitespace-nowrap">Use Backend API</span>
          </label>
          <label className="text-sm flex items-center gap-2">
            <span>Seed:</span>
            <input
              type="number"
              value={seed}
              onChange={(e) => setSeed(Number(e.target.value))}
              className="px-2 py-1 bg-gray-800 border border-gray-700 rounded text-white w-20 sm:w-24"
            />
          </label>
        </div>
      </div>

      {error && (
        <div className="bg-red-900/50 border border-red-700 text-red-200 px-4 py-2 rounded">
          <strong>Error:</strong> {error} (falling back to local simulation)
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 md:gap-6">
        {/* Left Panel: Request */}
        <div className="lg:col-span-3">
          <RequestPanel
            onRunOnce={handleRunOnce}
            onRunBatch={(req) => handleRunBatch(req, 100)}
            isLoading={useDecisionStore((s) => s.isLoading)}
            initialCategory={urlCategory || undefined}
            initialShow={urlShow || undefined}
            initialEpisode={urlEpisode || undefined}
          />
        </div>

        {/* Middle Panel: Pipeline */}
        <div className="lg:col-span-5">
          <PipelineStepper decision={currentDecision} />
        </div>

        {/* Right Panel: Auction Board */}
        <div className="lg:col-span-4">
          <AuctionBoard decision={currentDecision} />
          {currentDecision?.winner && (
            <button
              onClick={() => setShowExplain(true)}
              className="mt-4 w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded transition-colors"
            >
              Explain Winner
            </button>
          )}
        </div>
      </div>

      {/* Bottom: Metrics Strip */}
      <MetricsStrip 
        decision={currentDecision} 
        batchResults={useDecisionStore((s) => s.batchResults)}
      />

      {/* Explain Drawer */}
      {showExplain && currentDecision?.winner && (
        <ExplainDrawer
          decision={currentDecision}
          onClose={() => setShowExplain(false)}
        />
      )}
    </div>
  );
}

