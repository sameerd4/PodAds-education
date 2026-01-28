package com.podads.application.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class to extract and normalize brand names from campaign names.
 * Handles multi-word brands like "Capital One", "American Express", "Bank of America".
 */
public class BrandNameExtractor {
    
    // Multi-word brands that should be extracted as full names (check these first)
    private static final List<String> MULTI_WORD_BRANDS = Arrays.asList(
        "American Express", "Under Armour", "Bank of America", "Capital One",
        "The New York Times", "The Wall Street Journal", "The Washington Post", "The Guardian",
        "Apple TV+", "Apple TV", "HBO Max", "Paramount+", "Paramount Plus", 
        "Discovery+", "Discovery Plus", "YouTube Premium",
        "LinkedIn Learning", "Rosetta Stone", "Khan Academy"
    );
    
    // Mapping for normalizing partial brand names to full names
    private static final Map<String, String> BRAND_NORMALIZATION = Map.ofEntries(
        Map.entry("American", "American Express"),
        Map.entry("Under", "Under Armour"),
        Map.entry("Bank", "Bank of America"),
        Map.entry("Capital", "Capital One"),
        Map.entry("The New York", "The New York Times"),
        Map.entry("The Wall Street", "The Wall Street Journal"),
        Map.entry("The Washington", "The Washington Post"),
        Map.entry("HBO", "HBO Max"),
        Map.entry("Paramount", "Paramount Plus"),
        Map.entry("Paramount+", "Paramount Plus"),
        Map.entry("Discovery", "Discovery Plus"),
        Map.entry("Discovery+", "Discovery Plus"),
        Map.entry("YouTube", "YouTube Premium"),
        Map.entry("LinkedIn", "LinkedIn Learning"),
        Map.entry("Rosetta", "Rosetta Stone"),
        Map.entry("Khan", "Khan Academy")
    );
    
    /**
     * Extract brand name from campaign name.
     * Examples:
     * - "Capital One Venture - What's in Your Wallet?" -> "Capital One"
     * - "Nike Air Max - Just Do It" -> "Nike"
     * - "American Express Platinum - Don't Live Life Without It" -> "American Express"
     */
    public static String extractBrandName(String campaignName) {
        if (campaignName == null || campaignName.isEmpty()) {
            return null;
        }
        
        // Check multi-word brands first (longer matches first)
        for (String brand : MULTI_WORD_BRANDS) {
            if (campaignName.startsWith(brand)) {
                return brand;
            }
        }
        
        // Single-word brands (check longer/more specific variants first)
        String[] singleWordBrands = {
            "Nike", "Adidas", "Apple", "Samsung", "Chase", "Spotify", "Tesla", "Coursera",
            "Peloton", "Lululemon", "Gatorade", "Fitbit", "Reebok", "Puma", "Strava",
            "ESPN+", "ESPN", "Wilson", "Rawlings", "New Balance", "Mizuno",
            "Google", "Microsoft", "Meta", "Amazon", "Netflix", "Adobe",
            "PayPal", "Venmo", "Robinhood", "Fidelity", "Mastercard", "Visa",
            "Audible", "Hulu", "Peacock",
            "Comedy Central", "SiriusXM", "Pandora", "iHeartRadio",
            "CNN", "BBC", "Bloomberg", "Reuters", "NPR",
            "Udemy", "MasterClass", "Skillshare", "Duolingo", "Babbel", "edX"
        };
        
        for (String brand : singleWordBrands) {
            if (campaignName.startsWith(brand)) {
                return brand;
            }
        }
        
        return null;
    }
    
    /**
     * Normalize brand name to full name if it's a partial match.
     * Examples:
     * - "Capital" + "Capital One Venture..." -> "Capital One"
     * - "American" + "American Express Platinum..." -> "American Express"
     * - "Nike" -> "Nike" (no change)
     */
    public static String normalizeBrandName(String brandName, String campaignName) {
        if (brandName == null || brandName.isEmpty()) {
            return null;
        }
        
        // Check if brand name needs normalization
        String normalized = BRAND_NORMALIZATION.get(brandName);
        if (normalized != null && campaignName != null && campaignName.contains(normalized)) {
            return normalized;
        }
        
        // If brand name is already the full name, return it
        if (BRAND_NORMALIZATION.containsValue(brandName)) {
            return brandName;
        }
        
        return brandName;
    }
    
    /**
     * Extract and normalize brand name from campaign name in one step.
     * This is the main method to use for extracting brand names.
     */
    public static String extractAndNormalizeBrandName(String campaignName) {
        if (campaignName == null || campaignName.isEmpty()) {
            return null;
        }
        
        // First, try to extract the brand name
        String extractedBrand = extractBrandName(campaignName);
        
        if (extractedBrand != null) {
            // Normalize it to ensure we have the full name
            return normalizeBrandName(extractedBrand, campaignName);
        }
        
        // Fallback: extract first word and try to normalize it
        String firstWord = campaignName.split(" ")[0];
        return normalizeBrandName(firstWord, campaignName);
    }
}
