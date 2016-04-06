package com.onpositive.text.webview;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onpositive.text.webview.ui.FreeMarkerComponent;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Context.init(req);
		resp.setCharacterEncoding("UTF-8");
		PrintWriter writer = resp.getWriter();
		String queryString = req.getQueryString();
		writer.println(new WebSite().render());
		writer.close();
	}
}
