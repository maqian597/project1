package rpc;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import external.ExternalAPI;
import external.ExternalAPIFactory;
import external.TicketMasterAPI;
import entity.Item;

//import external.ExternalAPIFactory;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	/*protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		//Create a PrintWriter from response such that we can add data to response
		PrintWriter out = response.getWriter();
		if (request.getParameter("username") != null) {
			//Get the username sent from the client
			String username = request.getParameter("username");
			out.println("<html><body>");
			out.println("<h1>This is a HTML page<h1>");
			out.println("</body></html>");
			out.print("hello" + username);
		}
		out.println("<html><body>");
		out.println("<h1>This is a HTML page<h1>");
		out.println("</body></html>");
		out.flush();
		out.close();
	}*/
    /*
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");

		String username = "";
		if (request.getParameter("username") != null) {
			username = request.getParameter("username");
		}
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		try {
			//obj.put("username", username);
			array.put(new JSONObject().put("username", "lalalal"));
			array.put(new JSONObject().put("username", "1234455"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.print(array);
		out.flush();
		out.close();
	}*/
    /*
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Get parameter from HTTP request
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String term = request.getParameter("term"); // term can be null

		//old style
		// call TicketMasterAPI.search to get event data 
		//TicketMasterAPI tmAPI = new TicketMasterAPI();
		//List<Item> items = tmAPI.search(lat, lon, term);
		
		//change to ExternalAPI factory
		ExternalAPI api = ExternalAPIFactory.getExternalAPI();
		List<Item> items = api.search(lat, lon, term);
		// There should be some saveItem logic here

		// Convert Item list back to JSONArray for client
		List<JSONObject> list = new ArrayList<>();
		try {
			for (Item item : items) {
				// Add a thin version of restaurant object
				JSONObject obj = item.toJSONObject();
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = new JSONArray(items);
		RpcHelper.writeJsonArray(response, array);
	}*/

    //new updated doGet() function
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String term = request.getParameter("term"); // Term can be empty or null.
		
		DBConnection conn = DBConnectionFactory.getDBConnection(); //this is where the fatory used in code
		List<Item> items = conn.searchItems(userId, lat, lon, term);
		List<JSONObject> list = new ArrayList<>();
		Set<JSONObject> removeDup = new HashSet<>();
		Set<String> favorite = conn.getFavoriteItemIds(userId);
		try {
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				if (favorite != null) {
					obj.put("favorite", favorite.contains(item.getItemId()));
				}
				if (!removeDup.contains(obj)) {
					list.add(obj);
					removeDup.add(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = new JSONArray(list);
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
