import { useState } from 'react';
import { getLogoUrl } from '../lib/brandLogos';

interface BrandLogoProps {
  brandName?: string | null;
  campaignName?: string | null;
  size?: number;
  className?: string;
}

export default function BrandLogo({ 
  brandName, 
  campaignName, 
  size = 32,
  className = '' 
}: BrandLogoProps) {
  const [imageError, setImageError] = useState(false);
  const logoUrl = getLogoUrl(brandName, campaignName);
  
  // Fallback: show first letter of brand name
  const displayName = brandName || campaignName || '?';
  const initial = displayName.charAt(0).toUpperCase();
  
  if (!logoUrl || imageError) {
    return (
      <div 
        className={`flex items-center justify-center rounded bg-gray-600 text-white font-semibold flex-shrink-0 ${className}`}
        style={{ width: size, height: size, fontSize: size * 0.5 }}
      >
        {initial}
      </div>
    );
  }
  
  return (
    <div 
      className={`flex items-center justify-center rounded flex-shrink-0 ${className}`}
      style={{ 
        width: size, 
        height: size,
        padding: size * 0.15, // Add padding for better visibility
        backgroundColor: 'rgba(255, 255, 255, 0.95)', // White background for colored logos
      }}
    >
      <img
        src={logoUrl}
        alt={brandName || campaignName || 'Brand logo'}
        className="w-full h-full object-contain"
        onError={() => {
          console.warn(`Failed to load logo for ${brandName || campaignName} from ${logoUrl}`);
          setImageError(true);
        }}
        onLoad={() => {
          // Logo loaded successfully - remove any error state
          setImageError(false);
        }}
      />
    </div>
  );
}
