/**
 * Brand logo utility
 * Maps brand names to logo URLs
 */

// Extract brand name from campaign name (e.g., "Nike Air Max" -> "Nike")
// Also handles cases where brandName might be just the first word (e.g., "American" from "American Express")
export function extractBrandName(campaignName: string | undefined | null): string | null {
  if (!campaignName) return null;
  
  // Multi-word brands (check these first, including variations with +)
  const multiWordBrands = [
    'American Express', 'Under Armour', 'Bank of America', 'Capital One',
    'The New York Times', 'The Wall Street Journal', 'The Washington Post', 'The Guardian',
    'Apple TV+', 'Apple TV', 'HBO Max', 'Paramount+', 'Paramount Plus', 'Discovery+', 'Discovery Plus', 'YouTube Premium',
    'LinkedIn Learning', 'Rosetta Stone', 'Khan Academy'
  ];
  
  for (const brand of multiWordBrands) {
    if (campaignName.startsWith(brand)) {
      return brand;
    }
  }
  
  // Single-word brands (check longer/more specific variants first)
  const brandPrefixes = [
    'Nike', 'Adidas', 'Apple', 'Samsung', 'Chase', 'Spotify', 'Tesla', 'Coursera',
    'Peloton', 'Lululemon', 'Gatorade', 'Fitbit', 'Reebok', 'Puma', 'Strava',
    'ESPN+', 'ESPN', 'Wilson', 'Rawlings', 'New Balance', 'Mizuno',
    'Google', 'Microsoft', 'Meta', 'Amazon', 'Netflix', 'Adobe',
    'PayPal', 'Venmo', 'Robinhood', 'Fidelity', 'Mastercard', 'Visa',
    'Audible', 'Hulu', 'Peacock',
    'Comedy Central', 'SiriusXM', 'Pandora', 'iHeartRadio',
    'CNN', 'BBC', 'Bloomberg', 'Reuters', 'NPR',
    'Udemy', 'MasterClass', 'Skillshare', 'Duolingo', 'Babbel', 'edX'
  ];
  
  for (const brand of brandPrefixes) {
    if (campaignName.startsWith(brand)) {
      return brand;
    }
  }
  
  return null;
}

// Normalize brand name (e.g., "American" -> "American Express" if campaign suggests it)
export function normalizeBrandName(brandName: string | null | undefined, campaignName?: string | null): string | null {
  if (!brandName) return null;
  
  // Handle multi-word brand name normalization (including + variations)
  const multiWordMappings: Record<string, string> = {
    'American': 'American Express',
    'Under': 'Under Armour',
    'Bank': 'Bank of America',
    'Capital': 'Capital One',
    'The New York': 'The New York Times',
    'The Wall Street': 'The Wall Street Journal',
    'The Washington': 'The Washington Post',
    'The Guardian': 'The Guardian',
    'Apple TV': 'Apple TV',
    'Apple TV+': 'Apple TV',
    'HBO': 'HBO Max',
    'Paramount': 'Paramount Plus',
    'Paramount+': 'Paramount Plus',
    'Discovery': 'Discovery Plus',
    'Discovery+': 'Discovery Plus',
    'YouTube': 'YouTube Premium',
    'LinkedIn': 'LinkedIn Learning',
    'Rosetta': 'Rosetta Stone',
    'Khan': 'Khan Academy',
  };
  
  for (const [key, value] of Object.entries(multiWordMappings)) {
    // Check if brandName matches the key and campaign name contains the full brand name
    if (brandName === key && campaignName?.includes(value)) {
      return value;
    }
    // Also check if brandName is already the full name (e.g., "Capital One" when key is "Capital")
    // This handles cases where extraction already got the full name
    if (brandName === value) {
      return value;
    }
  }
  
  return brandName;
}

// Get logo URL for a brand name
// Uses Simple Icons CDN (reliable, no CORS issues)
// Falls back to alternative CDN for brands not in Simple Icons
export function getBrandLogoUrl(brandName: string | null | undefined): string | null {
  if (!brandName) return null;
  
  // Brands that need alternative CDN (not in Simple Icons)
  // Use favicon service for brands not available in Simple Icons
  const alternativeCdnBrands: Record<string, string> = {
    'Lululemon': 'lululemon.com',
    'Gatorade': 'gatorade.com',
    'Amazon': 'amazon.com',
    'Amazon Prime': 'amazon.com',
    'Microsoft': 'microsoft.com',
    'Adobe': 'adobe.com',
    'Capital': 'capitalone.com',  // Handle "Capital" (normalized from "Capital One")
    'Capital One': 'capitalone.com',
    'Fidelity': 'fidelity.com',
    'Hulu': 'hulu.com',
    'Peacock': 'peacocktv.com',
    'Discovery Plus': 'discoveryplus.com',
    'Discovery+': 'discoveryplus.com',
    'ESPN+': 'espn.com',
    'Mizuno': 'mizuno.com',
    'Wilson': 'wilson.com',
    'Rawlings': 'rawlings.com',
    'Comedy Central': 'comedycentral.com',
    'BBC': 'bbc.com',
    'Reuters': 'reuters.com',
    'NPR': 'npr.org',
    'The New York Times': 'nytimes.com',
    'The Wall Street Journal': 'wsj.com',
    'Bloomberg': 'bloomberg.com',
    'MasterClass': 'masterclass.com',
    'LinkedIn Learning': 'linkedin.com',
    'Babbel': 'babbel.com',
    'Rosetta Stone': 'rosettastone.com',
  };
  
  // Check if brand needs alternative CDN
  if (alternativeCdnBrands[brandName]) {
    const domain = alternativeCdnBrands[brandName];
    // Easter egg: Use Windows XP logo for Microsoft (colored version from Pinterest)
    if (brandName === 'Microsoft') {
      return `https://i.pinimg.com/736x/9e/a8/ac/9ea8ac9376266cc8653cecf91cf373b7.jpg`;
    }
    // Use direct favicon for Capital One (Google favicon service doesn't work well for it)
    if (brandName === 'Capital One' || brandName === 'Capital') {
      return `https://www.capitalone.com/favicon.ico`;
    }
    // Use favicon service for other brands (reliable, no API key needed)
    // This provides the brand's favicon which is usually their logo
    // Note: Some brands may need direct favicon URLs if Google service doesn't work
    return `https://www.google.com/s2/favicons?domain=${domain}&sz=256`;
  }
  
  // Map brand names to Simple Icons identifiers
  const brandLogoMap: Record<string, string> = {
    'Nike': 'nike',
    'Adidas': 'adidas',
    'Under Armour': 'underarmour',
    'Peloton': 'peloton',
    'Fitbit': 'fitbit',
    'Reebok': 'reebok',
    'Puma': 'puma',
    'Strava': 'strava',
    'ESPN': 'espn',
    'ESPN+': 'espn',
    'Wilson': 'wilson',
    'Rawlings': 'rawlings',
    'New Balance': 'newbalance',
    'Mizuno': 'mizuno',
    'Apple': 'apple',
    'Samsung': 'samsung',
    'Google': 'google',
    'Microsoft': 'microsoft',
    'Meta': 'meta',
    'Amazon': 'amazon',
    'Netflix': 'netflix',
    'Adobe': 'adobe',
    'Spotify': 'spotify',
    'Tesla': 'tesla',
    'Chase': 'chase',
    'American Express': 'americanexpress',
    'Capital One': 'capitalone',
    'Bank of America': 'bankofamerica',
    'PayPal': 'paypal',
    'Venmo': 'venmo',
    'Robinhood': 'robinhood',
    'Fidelity': 'fidelity',
    'Mastercard': 'mastercard',
    'Visa': 'visa',
    'Audible': 'audible',
    'Amazon Prime': 'amazonprime',
    'HBO Max': 'hbomax',
    'Paramount Plus': 'paramountplus',
    'Paramount+': 'paramountplus',
    'Discovery Plus': 'discoveryplus',
    'Discovery+': 'discoveryplus',
    'Hulu': 'hulu',
    'Peacock': 'peacock',
    'Apple TV': 'appletv',
    'Apple TV+': 'appletv',
    'Comedy Central': 'comedycentral',
    'SiriusXM': 'siriusxm',
    'Pandora': 'pandora',
    'iHeartRadio': 'iheartradio',
    'YouTube Premium': 'youtube',
    'The New York Times': 'thenewyorktimes',
    'The Wall Street Journal': 'thewallstreetjournal',
    'CNN': 'cnn',
    'BBC': 'bbc',
    'Bloomberg': 'bloomberg',
    'Reuters': 'reuters',
    'NPR': 'npr',
    'The Washington Post': 'thewashingtonpost',
    'The Guardian': 'theguardian',
    'Coursera': 'coursera',
    'Udemy': 'udemy',
    'Khan Academy': 'khanacademy',
    'MasterClass': 'masterclass',
    'LinkedIn Learning': 'linkedin',
    'Skillshare': 'skillshare',
    'Duolingo': 'duolingo',
    'Babbel': 'babbel',
    'Rosetta Stone': 'rosettastone',
    'edX': 'edx',
  };
  
  const simpleIconName = brandLogoMap[brandName];
  if (!simpleIconName) {
    // Brands not in Simple Icons will fall back to initial letter in BrandLogo component
    return null;
  }
  
  // Use Simple Icons CDN with brand's primary color (colored logos!)
  // Format: https://cdn.simpleicons.org/{brand}/{color}
  const brandColors: Record<string, string> = {
    'Nike': '000000', 'Adidas': '000000', 'Under Armour': '000000', 'Reebok': '000000',
    'Puma': '000000', 'Apple': '000000', 'Apple TV': '000000', 'Apple TV+': '000000',
    'Peloton': '000000', 'Lululemon': '000000', 'New Balance': '000000',
    'Gatorade': 'FF6900', 'Fitbit': '00B0B9', 'Strava': 'FC4C02',
    'ESPN': '000000', 'ESPN+': '000000', 'Wilson': '000000', 'Rawlings': '000000', 'Mizuno': '000000',
    'Samsung': '1428A0', 'Google': '4285F4', 'Microsoft': '0078D4', 'Meta': '0081FB',
    'Amazon': 'FF9900', 'Amazon Prime': '00A8E1', 'Netflix': 'E50914', 'Adobe': 'FF0000',
    'Spotify': '1DB954', 'Tesla': 'CC0000',
    'Chase': '117ACA', 'American Express': '006FCF', 'Capital One': '004977',
    'Bank of America': 'E31837', 'PayPal': '003087', 'Venmo': '3D95CE',
    'Robinhood': '00C805', 'Fidelity': '0175C2', 'Mastercard': 'EB001B', 'Visa': '1A1F71',
    'Audible': 'F8991C', 'HBO Max': '000000', 'Paramount Plus': '0072FF', 'Paramount+': '0072FF',
    'Discovery Plus': '000000', 'Discovery+': '000000', 'Hulu': '1CE783', 'Peacock': '000000',
    'Comedy Central': '000000', 'SiriusXM': '000000', 'Pandora': '224099',
    'iHeartRadio': 'C6002B', 'YouTube Premium': 'FF0000',
    'The New York Times': '000000', 'The Wall Street Journal': '000000', 'CNN': 'CC0000',
    'BBC': '000000', 'Bloomberg': '000000', 'Reuters': 'FF8000',
    'NPR': 'C21807', 'The Washington Post': '000000', 'The Guardian': '052962',
    'Coursera': '0056D2', 'Udemy': 'A435F0', 'Khan Academy': '14BF96',
    'MasterClass': '000000', 'LinkedIn Learning': '0077B5', 'Skillshare': '00FF84',
    'Duolingo': '58CC02', 'Babbel': '00B0F0', 'Rosetta Stone': '0098DA', 'edX': '02262B',
  };
  
  const color = brandColors[brandName] || 'FFFFFF'; // Default to white if no color specified
  return `https://cdn.simpleicons.org/${simpleIconName}/${color}`;
}

// Get logo URL from brand name or campaign name
export function getLogoUrl(brandName: string | null | undefined, campaignName?: string | null): string | null {
  // Normalize brand name first (handles "American" -> "American Express")
  const normalizedBrand = normalizeBrandName(brandName, campaignName);
  
  // Try normalized brand name first
  if (normalizedBrand) {
    const url = getBrandLogoUrl(normalizedBrand);
    if (url) return url;
  }
  
  // Fallback: extract brand from campaign name
  if (campaignName) {
    const extractedBrand = extractBrandName(campaignName);
    if (extractedBrand) {
      return getBrandLogoUrl(extractedBrand);
    }
  }
  
  return null;
}
