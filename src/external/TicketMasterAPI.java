package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;
import external.ExternalAPI;

public class TicketMasterAPI implements ExternalAPI{
	private static final String API_HOST = "app.ticketmaster.com";
	private static final String SEARCH_PATH = "/discovery/v2/events.json";
	private static final String DEFAULT_TERM = "";  // no restriction
	private static final String API_KEY = "MUxYMMKVXLgHQBvcOuqtW12CCHlHClVv";
	@Override
	public List<Item> search(double lat, double lon, String term) {
		//return null;
        // create a base url, based on API_HOST and SEARCH_PATH
		String url = "http://" + API_HOST + SEARCH_PATH;
		
		// Convert geo location to geo hash with a precision of 4 (+- 20km)
		String geoHash = GeoHash.encodeGeohash(lat, lon, 4);
		if (term == null) {
			term = DEFAULT_TERM;
		}
		// Encode term in url since it may contain special characters
		term = urlEncodeHelper(term);
		// Make your url query part like: "apikey=12345&geoPoint=abcd&keyword=music"
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=50", API_KEY, geoHash, term);
		try {
			// Create a HTTP connection between your Java application and TicketMaster based on url
			HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();
			// Set request method to GET
			connection.setRequestMethod("GET");
			
			// Send request to TicketMaster and get response, response code could be returned directly
			// response body is saved in InputStream of connection.
			
			//responseCode is the function of class HttpURLConnection
			int responseCode = connection.getResponseCode();
			
			//for debug 
			System.out.println("\nSending 'GET' request to URL : " + url + "?" + query);
			System.out.println("Response Code : " + responseCode);
			
			// Now read response body to get events data
			//different return context store in connection.getResponseCode() and connection.getInputStream();
			//Synchronous  jiudengzhe  using buffer reader to copy the stream;
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			//string builder copy is efficent than string copy
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject responseJson = new JSONObject(response.toString());
			//this json object has three _link, _embedded, page, we only need the _embedded's events 
			//_embedded is object, events is array of item  can read document in TM website.
			JSONObject object= (JSONObject) responseJson.get("_embedded");
			JSONArray array= (JSONArray) object.get("events");
			//return array;
			return getItermList(array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
}

	private String urlEncodeHelper(String term) {
		try {
			term = java.net.URLEncoder.encode(term, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return term;
	}
	private void queryAPI(double lat, double lon) {
		/*
		JSONArray events = search(lat, lon, null);
		try {
		    for (int i = 0; i < events.length(); i++) {
		        JSONObject event = events.getJSONObject(i);
		        System.out.println(event);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		List<Item> itermList = search(lat, lon, null);
		try {
			for (Item iterm : itermList) {
				JSONObject jsonObject = iterm.toJSONObject();
				System.out.println(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	// Convert JSONArray to a list of item objects.
		private List<Item> getItermList(JSONArray events) throws JSONException {
			List<Item> itemList = new ArrayList<>();
			System.out.println("events =" + events);
			for (int i = 0; i < events.length(); i++) {
				JSONObject event = events.getJSONObject(i);
				ItemBuilder builder = new ItemBuilder();
				builder.setItemId(getStringFieldOrNull(event, "id"));
				builder.setName(getStringFieldOrNull(event, "name"));
				builder.setDescription(getDescription(event));
				builder.setCategories(getCategories(event));
				builder.setImageUrl(getImageUrl(event));
				builder.setUrl(getStringFieldOrNull(event, "url"));
				
				JSONObject venue = getVenue(event);
				if (venue != null) {
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						StringBuilder sb = new StringBuilder();
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(address.getString("line3"));
						}
						builder.setAddress(sb.toString());
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						builder.setCity(getStringFieldOrNull(city, "name"));
					}
					if (!venue.isNull("country")) {
						JSONObject country = venue.getJSONObject("country");
						builder.setCountry(getStringFieldOrNull(country, "name"));
					}
					if (!venue.isNull("state")) {
						JSONObject state = venue.getJSONObject("state");
						builder.setState(getStringFieldOrNull(state, "name"));
					}
					builder.setZipcode(getStringFieldOrNull(venue, "postalCode"));
					if (!venue.isNull("location")) {
						JSONObject location = venue.getJSONObject("location");
						builder.setLatitude(getNumericFieldOrNull(location, "latitude"));
						builder.setLongitude(getNumericFieldOrNull(location, "longitude"));
					}
				}

				// Uses this builder pattern we can freely add fields.
				// convert builder to item, call build function
				Item item = builder.build();
				itemList.add(item);
			}

			return itemList;
		}

		 // return the first venue of an event object
		private JSONObject getVenue(JSONObject event) throws JSONException {
			if (!event.isNull("_embedded")) {
				JSONObject embedded = event.getJSONObject("_embedded");
				if (!embedded.isNull("venues")) {
					JSONArray venues = embedded.getJSONArray("venues");
					if (venues.length() >= 1) {
						return venues.getJSONObject(0);
					}
				}
			}
			return null;
		}

	    // Get first image url from an event object
		private String getImageUrl(JSONObject event) throws JSONException {
			/*if (!event.isNull("_embedded")) {
				String url = event.getString("url");
				return url;
			}
			return null;*/
			if (!event.isNull("images")) {
				JSONArray imagesArray = event.getJSONArray("images");
				if (imagesArray.length() >= 1) {
					return getStringFieldOrNull(imagesArray.getJSONObject(0), "url" );
				}
			}
			return null;

		}

		private Set<String> getCategories(JSONObject event) throws JSONException {
			Set<String> categories = new HashSet<>();
			/*
			JSONArray classifications = (JSONArray) event.get("classifications");
			//System.out.println(classifications);
			//because classification just a item array with dictonary with key primary, segment, genre, and son on
			//why ?
			//JSONObject segment = (JSONObject) classifications.get("segment"); this is not corret;
			JSONObject segment = ((JSONObject) classifications.get(0)).getJSONObject("segment");
			//System.out.println(segment);
			String category = (String) segment.get("name");
			categories.add(category);*/
			//JsonArray is a array of JsonObject
			if (!event.isNull("classifications")) {
					JSONArray classifications = (JSONArray) event.get("classifications");
					for (int j = 0; j < classifications.length(); j++) {
						JSONObject classification = classifications.getJSONObject(j);
						JSONObject segment = classification.getJSONObject("segment");
						categories.add(segment.getString("name"));
					}
				}
			return categories;
		}

		private String getDescription(JSONObject event) throws JSONException {
			if (!event.isNull("description")) {
				return event.getString("description");
			} else if (!event.isNull("additionalInfo")) {
				return event.getString("additionalInfo");
			} else if (!event.isNull("info")) {
				return event.getString("info");
			} else if (!event.isNull("pleaseNote")) {
				return event.getString("pleaseNote");
			}
			return null;
		}

		private String getStringFieldOrNull(JSONObject event, String field) throws JSONException {
			return event.isNull(field) ? null : event.getString(field);
		}

		private double getNumericFieldOrNull(JSONObject event, String field) throws JSONException {
			return event.isNull(field) ? 0.0 : event.getDouble(field);
		}

	
	/**
	 * Main entry for sample TicketMaster API requests.
	 */
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		//tmApi.queryAPI(37.38, -122.08);
		// London, UK
		//tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
		//can get lat lon javascript to get the mac 
		//or call google map API to get the lat lon. when input is a location
	}


}
