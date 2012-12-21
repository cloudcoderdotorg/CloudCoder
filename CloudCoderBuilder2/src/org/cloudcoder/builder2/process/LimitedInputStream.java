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

package org.cloudcoder.builder2.process;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.CountingInputStream;

/**
 * An InputStream which allows only a specified maximum number
 * of bytes to be read from the underlying (wrapped)
 * InputStream.  Note that the maximum is not enforced
 * precisely, and may exceed the specified maximum by
 * the size of the buffer used by {@link CountingInputStream}.
 * 
 * Throws {@link InputAmountExceededException}, a subclass of
 * IOException, when too much input has been read.
 * 
 * @author David Hovemeyer
 */
public class LimitedInputStream extends CountingInputStream {
	private static class TooMuchInputException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	private final int maxBytesAllowed;

	/**
	 * Constructor.
	 * 
	 * @param in               the InputStream to read from
	 * @param maxBytesAllowed  the maximum number of bytes which may be
	 *                         read from the input stream
	 */
	public LimitedInputStream(InputStream in, int maxBytesAllowed) {
		super(in);
		this.maxBytesAllowed = maxBytesAllowed;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.io.input.CountingInputStream#afterRead(int)
	 */
	@Override
	protected synchronized void afterRead(int n) {
		if (getCount() + n > maxBytesAllowed) {
			throw new TooMuchInputException();
		}
		super.afterRead(n);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.io.input.ProxyInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		try {
			return super.read();
		} catch (TooMuchInputException e) {
			throw new InputAmountExceededException("Input amount exceeded");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.io.input.ProxyInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] bts) throws IOException {
		try {
			return super.read(bts);
		} catch (TooMuchInputException e) {
			throw new InputAmountExceededException("Input amount exceeded");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.io.input.ProxyInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] bts, int off, int len) throws IOException {
		try {
			return super.read(bts, off, len);
		} catch (TooMuchInputException e) {
			throw new InputAmountExceededException("Input amount exceeded");
		}
	}
}
