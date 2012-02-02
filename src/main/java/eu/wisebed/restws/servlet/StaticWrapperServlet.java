package eu.wisebed.restws.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

@Singleton
public class StaticWrapperServlet extends HttpServlet {
	private static final long serialVersionUID = -8713025171082670524L;

	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
		HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
			public String getPathInfo() {
				return null;
			}
		};
		rd.forward(wrapped, resp);
	}
}