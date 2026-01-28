import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { DecisionTrace } from '@podads/shared';
import PipelineStepper from '../components/PipelineStepper';
import AuctionBoard from '../components/AuctionBoard';

export default function TracePage() {
  const { decisionId } = useParams<{ decisionId: string }>();
  const [trace] = useState<DecisionTrace | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // TODO: Load trace from API or local storage
    // For now, show placeholder
    setLoading(false);
  }, [decisionId]);

  if (loading) {
    return <div className="text-center py-12">Loading trace...</div>;
  }

  if (!trace) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold mb-4">Trace Not Found</h1>
        <p className="text-gray-400">
          Decision ID: <code className="bg-gray-800 px-2 py-1 rounded">{decisionId}</code>
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Trace Replay</h1>
        <p className="text-gray-400 mt-2">
          Decision ID: <code className="bg-gray-800 px-2 py-1 rounded">{decisionId}</code>
        </p>
      </div>

      <div className="grid grid-cols-12 gap-6">
        <div className="col-span-8">
          <PipelineStepper decision={trace.decision} />
        </div>
        <div className="col-span-4">
          <AuctionBoard decision={trace.decision} />
        </div>
      </div>
    </div>
  );
}

