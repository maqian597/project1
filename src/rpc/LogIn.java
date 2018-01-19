package rpc;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class LogIn
 */
@WebServlet("/login")
public class LogIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final DBConnection cnn = DBConnectionFactory.getDBConnection();  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject msg = new JSONObject();
			HttpSession session = request.getSession();
			if (session.getAttribute("user_id") == null) {
				
				response.setStatus(403);
				msg.put("status", "Session Invalid");

			} else {
				String userId = (String) session.getAttribute("user_id");
				String name = cnn.getFullname(userId);
				session.setAttribute("name", name);
				msg.put("status", "OK");
				msg.put("user_id", userId);
				msg.put("full_name", name);
			}
			System.out.println(msg);
			RpcHelper.writeJsonObject(response, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject msg = new JSONObject();
			//get request form for userId and password
			String userId = request.getParameter("user_id");
			String pwd = request.getParameter("password");
			if (cnn.verifyLogin(userId, pwd)) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(20*60); //set maximum time is 20min.
				//get user name
				String name = cnn.getFullname(userId);
				msg.put("status", "OK");
				msg.put("user_id", userId);
				msg.put("full_name", name);
			} else {
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
