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

package org.cloudcoder.app.server.persist.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;

/**
 * Variant of {@link AbstractDatabaseRunnable} whose run() method is guaranteed
 * not to throw {@link CloudCoderAuthenticationException}.  Subclass this for
 * database transactions that don't require user authentication.
 * 
 * @author David Hovemeyer
 *
 * @param <E>
 */
public abstract class AbstractDatabaseRunnableNoAuthException<E> extends AbstractDatabaseRunnable<E> implements DatabaseRunnable<E> {
	@Override
	public abstract E run(Connection conn) throws SQLException;
}
