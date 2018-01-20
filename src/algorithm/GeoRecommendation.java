package algorithm;

import java.util.*;
import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		DBConnection conn = DBConnectionFactory.getDBConnection();

		// step 1 db queries
		Set<String> favoriteItems = conn.getFavoriteItemIds(userId); 
		//step 2 get all possible categories especially Undefined.
		Set<String> allCategories = new HashSet<>(); 
		for (String item : favoriteItems) {
			allCategories.addAll(conn.getCategories(item)); //db queries
		}
		//remove undefined category, that's the item has no classifiers.
		allCategories.remove("Undefined"); 
		if (allCategories.isEmpty()) {
			allCategories.add("");
		}
		//step 3 search for all possible items.
		Set<Item> recommendedItems = new HashSet<>(); 
		for (String category : allCategories) {
			List<Item> items = conn.searchItems(userId, lat, lon, category); // call external API
			recommendedItems.addAll(items);
		}

		// why we use list now instead of set? because we will have ranking now.
		// step 4 
		List<Item> filteredItems = new ArrayList<>();  
		for (Item item : recommendedItems) {
			if (!favoriteItems.contains(item.getItemId())) {
				filteredItems.add(item);
			}
		}

		// step 5. perform ranking of these items based on distance.
		Collections.sort(filteredItems, new Comparator<Item>() {
		@Override
		public int compare(Item item1, Item item2) {
			// We can improved the rank calculator here.
			double distance1 = getDistance(item1.getLatitude(), item1.getLongitude(), lat, lon);
			double distance2 = getDistance(item2.getLatitude(), item2.getLongitude(), lat, lon);
			// return the increasing order of distance.
			return (int) (distance1 - distance2);}
});

		return filteredItems;
	}

	// Calculate the distances between two geolocations.
	// Source : http://andrew.hedges.name/experiments/haversine/
	private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;
		double a = Math.sin(dlat / 2 / 180 * Math.PI) * Math.sin(dlat / 2 / 180 * Math.PI)
				+ Math.cos(lat1 / 180 * Math.PI) * Math.cos(lat2 / 180 * Math.PI)
				* Math.sin(dlon / 2 / 180 * Math.PI) * Math.sin(dlon / 2 / 180 * Math.PI);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		// Radius of earth in miles.
		double R = 3961;
		return R * c;
	}

}
