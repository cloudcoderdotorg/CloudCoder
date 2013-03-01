// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.server.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Utility methods for admin filters and servlets.
 * 
 * @author David Hovemeyer
 */
public abstract class AdminServletUtil {

	/**
	 * Send back a 400 (Bad Request) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @throws IOException
	 */
	public static void badRequest(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Invalid request");
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * Send back a 401 (Unauthorized) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @throws IOException
	 */
	public static void unauthorized(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Authorization required");
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resp.addHeader("WWW-Authenticate", "Basic realm=\"" + AdminAuthorizationFilter.CLOUDCODER_ADMIN_REALM_NAME + "\"");
	}

	/**
	 * Send back a 403 (forbidden) response.
	 * 
	 * @param resp  the HttpServletResponse
	 * @throws IOException
	 */
	public static void forbidden(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("You are not authorized to access this resource");
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}

}
