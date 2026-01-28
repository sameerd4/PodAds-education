import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PodcastCategory } from '@podads/shared';

const scenarios = [
  { 
    category: 'fitness' as PodcastCategory, 
    name: 'Fitness & Health', 
    description: 'Running, workouts, nutrition',
    show: 'The Daily Run',
    episode: 'Episode 42: Morning Motivation'
  },
  { 
    category: 'tech' as PodcastCategory, 
    name: 'Technology', 
    description: 'Gadgets, software, startups',
    show: 'Tech Talk Weekly',
    episode: 'Episode 142: AI Revolution'
  },
  { 
    category: 'finance' as PodcastCategory, 
    name: 'Finance', 
    description: 'Investing, banking, credit cards',
    show: 'Money Matters',
    episode: 'Episode 88: Market Insights'
  },
  { 
    category: 'true-crime' as PodcastCategory, 
    name: 'True Crime', 
    description: 'Mysteries, investigations',
    show: 'Cold Case Files',
    episode: 'Episode 203: The Vanishing'
  },
  { 
    category: 'sports' as PodcastCategory, 
    name: 'Sports', 
    description: 'Games, athletes, analysis',
    show: 'Game Day Breakdown',
    episode: 'Episode 67: Championship Recap'
  },
];

export default function ScenariosPage() {
  const navigate = useNavigate();
  const [selectedSeed, setSelectedSeed] = useState(12345);

  const handleLoadScenario = (scenario: typeof scenarios[0]) => {
    // Navigate to live page with scenario pre-filled
    const params = new URLSearchParams({
      category: scenario.category,
      seed: selectedSeed.toString(),
      show: scenario.show,
      episode: scenario.episode,
    });
    navigate(`/live?${params.toString()}`);
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <h1 className="text-2xl sm:text-3xl font-bold">Scenario Library</h1>
        <div className="flex items-center gap-2 sm:gap-4">
          <label className="text-sm flex items-center gap-2">
            <span>Seed:</span>
            <input
              type="number"
              value={selectedSeed}
              onChange={(e) => setSelectedSeed(Number(e.target.value))}
              className="px-2 py-1 bg-gray-800 border border-gray-700 rounded text-white w-20 sm:w-24"
            />
          </label>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {scenarios.map((scenario) => (
          <div
            key={scenario.category}
            className="bg-gray-800 rounded-lg p-6 hover:bg-gray-700 transition-colors cursor-pointer"
            onClick={() => handleLoadScenario(scenario)}
          >
            <h2 className="text-xl font-semibold mb-2">{scenario.name}</h2>
            <p className="text-gray-400 text-sm mb-2">{scenario.description}</p>
            <p className="text-gray-500 text-xs mb-4 italic">{scenario.show}</p>
            <button className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded transition-colors">
              Load Scenario
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

