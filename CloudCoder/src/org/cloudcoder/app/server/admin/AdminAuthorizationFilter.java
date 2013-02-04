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
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.rpc.SessionAttributeKeys;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for Filters to check that the client is authorized
 * to use an admin servlet.
 * Uses the user present in the server-side session if there is one.
 * Otherwise, checks that Basic HTTP authorization has
 * been provided and that it matches a CloudCoder user.  Adds the
 * User object to the request as a request attribute.  Delegates
 * to a subclass to check that the client is authorized to perform
 * whatever specific kind of request the servlet protected by the
 * filter is handling.
 * 
 * @author David Hovemeyer
 * @see http://www.httpwatch.com/httpgallery/authentication/
 */
public abstract class AdminAuthorizationFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(AdminAuthorizationFilter.class);
	
	static final String CLOUDCODER_ADMIN_REALM_NAME = "CloudCoderAdmin";

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req_, ServletResponse resp_, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) req_;
		HttpServletResponse resp = (HttpServletResponse) resp_;

		User user;
		
		// Authenticate user.
		// If there is a user in the session, use it.
		// (This handles the case of AJAX requests made by the
		// client-side webapp.)
		// Otherwise, do basic HTTP authentication.
		user = (User) req.getSession().getAttribute(SessionAttributeKeys.USER_KEY);
		if (user == null) {
			user = doBasicAuth(req, resp);
		}
		
		if (user != null) {
			req.setAttribute(RequestAttributeKeys.USER_KEY, user);
			
			// Delegate to subclass to do servlet-specific checks.
			checkAuthorization(user, req, resp, chain);
		}
	}
	
	private User doBasicAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String authHeader = req.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Basic ")) {
			AdminServletUtil.unauthorized(resp);
			return null;
		}
		
		// Extract the base64-encoded username:password combo.
		String authString = authHeader.substring("Basic ".length()).trim();
		byte[] bytes;
		try {
			bytes = DatatypeConverter.parseBase64Binary(authString);
		} catch (IllegalArgumentException e) {
			logger.info("Admin auth: invalid base64 data " + authString);
			AdminServletUtil.unauthorized(resp);
			return null;
		}
		String userNameAndPassword = new String(bytes, Charset.forName("UTF-8"));
		int sep = userNameAndPassword.indexOf(':');
		if (sep < 0) {
			logger.info("Admin auth: invalid username:password pair");
			AdminServletUtil.unauthorized(resp);
			return null;
		}
		
		// Extract username and password
		String userName = userNameAndPassword.substring(0, sep);
		String password = userNameAndPassword.substring(sep+1);
		
		// Look up user in database, ensure that password matches.
		// FIXME: This only works with database authentication
		// authentication with imap requires use of web.xml
		// which filter cannot access
		User user = Database.getInstance().authenticateUser(userName, password);
		if (user == null) {
			logger.info("Admin auth: username/password mismatch for " + userName);
			AdminServletUtil.unauthorized(resp);
			return null;
		}

		return user;
	}

	/**
	 * Check whether the given User is authorized to perform the action
	 * requested in the given HttpServletRequest.  Should be overridden
	 * by subclasses to perform whatever sort of authorization is
	 * appropriate for the servlet being protected by this Filter. 
	 * 
	 * @param user  the User
	 * @param req   the HttpServletRequest
	 * @param resp  the HttpServletResponse
	 * @param chain the FilterChain
	 */
	protected abstract void checkAuthorization(
			User user, HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
			throws IOException, ServletException ;

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// nothing to do
	}
}
