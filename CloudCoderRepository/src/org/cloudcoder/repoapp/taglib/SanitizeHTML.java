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

package org.cloudcoder.repoapp.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.owasp.html.Handler;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;

import com.google.common.base.Function;

/**
 * Custom JSP tag to sanitize HTML by allowing only a limited
 * set of tags and attributes to be used.
 * 
 * @author David Hovemeyer
 */
public class SanitizeHTML extends TagSupport {
	private static final long serialVersionUID = 1L;
	
	// This is adapted from SlashdotPolicyExample.java in the owasp-java-html-sanitizer
	// distribution.  This is an even more restrictive policy.
	private static final Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> POLICY =
			new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				// Allow href="..." on <a> elements.
				.allowAttributes("href").onElements("a")
				// Defeat link spammers.
				.requireRelNofollowOnLinks()
				// Allowed elements
				.allowElements(
					"a", "p", "div", "i", "b", "em", "blockquote", "tt", "strong",
					"br", "ul", "ol", "li", "pre", "code")
				.toFactory();

	private String html;

	@Override
	public int doStartTag() throws JspException {
		JspWriter out = pageContext.getOut();

		// Sanitize the HTML.
		// If an error occurs, either because we couldn't write
		// to the JSP output stream, or because the HTML couldn't
		// be sanitized, we just do nothing and stop.
		HtmlStreamRenderer renderer = HtmlStreamRenderer.create(
				out,
				
				// Receives notifications on a failure to write to the output.
				new Handler<IOException>() {
					public void handle(IOException ex) {
						throw new RuntimeException();
					}
				},
				
				// Our HTML parser is very lenient, but this receives notifications on
				// truly bizarre inputs.
				new Handler<String>() {
					public void handle(String x) {
						throw new RuntimeException();
					}
				});
		
		try {
			// Use the policy defined above to sanitize the HTML.
			HtmlSanitizer.sanitize(html, POLICY.apply(renderer));
		} catch (RuntimeException e) {
			// Something went wrong writing the sanitized output.
		}

		return SKIP_BODY;
	}

	public void setHtml(String html) {
		this.html = html;
	}
}
