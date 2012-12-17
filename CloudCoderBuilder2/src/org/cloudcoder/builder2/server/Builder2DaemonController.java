// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.builder2.server;

import java.io.IOException;

import org.cloudcoder.daemon.DaemonController;
import org.cloudcoder.daemon.IDaemon;
import org.cloudcoder.daemon.JarRewriter;

/**
 * {@link DaemonController} implementation for the Builder.
 * Also contains the main method used when the Builder is deployed
 * as an executable jar file.
 * 
 * @author David Hovemeyer
 */
public class Builder2DaemonController extends DaemonController {

	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#getDefaultInstanceName()
	 */
	@Override
	public String getDefaultInstanceName() {
		return "instance";
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#getDaemonClass()
	 */
	@Override
	public Class<? extends IDaemon> getDaemonClass() {
		return Builder2Daemon.class;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.daemon.DaemonController#createOptions()
	 */
	@Override
	protected Options createOptions() {
		// Create the stdout log in the "log" directory.
		return new Options() {
			@Override
			public String getStdoutLogFileName() {
				return "log/stdout.log";
			}
		};
	}

	public static void main(String[] args) throws IOException {
		if (args.length >= 1 && args[0].equals("configure")) {
			// Allow the builder jarfile to be updated
			// (e.g., to add/replace cloudcoder.properties or keystore.jks)
			JarRewriter jarRewriter = null;
			for (int i = 1; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("--editJar=")) {
					arg = arg.substring("--editJar=".length());
					jarRewriter = new JarRewriter(arg);
				} else if (arg.startsWith("--replace=")) {
					arg = arg.substring("--replace=".length());
					int eq = arg.indexOf('=');
					String entry = arg.substring(0, eq);
					String fileName = arg.substring(eq + 1);
					jarRewriter.replaceEntry(entry, new JarRewriter.FileEntryData(fileName));
				}
			}
			jarRewriter.rewrite();
		} else {
			Builder2DaemonController controller = new Builder2DaemonController();
			controller.exec(args);
		}
	}
}
