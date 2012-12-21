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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * IOutputCollector implementation that captures only a limited amount
 * of output from a running process.  The idea is to prevent
 * tested processes from generating huge amounts of output and
 * crashing the Builder, swamping the database, etc.
 * 
 * @author David Hovemeyer
 */
public class LimitedOutputCollector implements IOutputCollector {
	/** Default maximum number of bytes allowed. */
	public static final int DEFAULT_MAX_BYTES_ALLOWED = 8192;
	
	/** Default maximum number of lines of text allowed. */
	public static final int DEFAULT_MAX_LINES_ALLOWED = 60;
	
	/** Default maximum number of characters per line. */
	public static final int DEFAULT_MAX_CHARACTERS_PER_LINE = 80;
	
	private int maxBytesAllowed;
	private int maxLinesAllowed;
	private int maxCharactersPerLine;
	
	private InputStream in;
	private Reader reader;
	private Thread thread;
	private List<String> collectedLines;
	
	/**
	 * Constructor.
	 * Maximum numbers of bytes, lines, and characters per line
	 * are all set to the default values.
	 * 
	 * @param in the InputStream to read from
	 */
	public LimitedOutputCollector(InputStream in) {
		this.maxBytesAllowed = DEFAULT_MAX_BYTES_ALLOWED;
		this.maxLinesAllowed = DEFAULT_MAX_LINES_ALLOWED;
		this.maxCharactersPerLine = DEFAULT_MAX_CHARACTERS_PER_LINE;
		
		this.in = in;
		
		this.collectedLines = new ArrayList<String>();
	}
	
	/**
	 * @param maxBytesAllowed the maxBytesAllowed to set
	 */
	public void setMaxBytesAllowed(int maxBytesAllowed) {
		this.maxBytesAllowed = maxBytesAllowed;
	}
	
	/**
	 * @param maxLinesAllowed the maxLinesAllowed to set
	 */
	public void setMaxLinesAllowed(int maxLinesAllowed) {
		this.maxLinesAllowed = maxLinesAllowed;
	}
	
	/**
	 * @param maxCharactersPerLine the maxCharactersPerLine to set
	 */
	public void setMaxCharactersPerLine(int maxCharactersPerLine) {
		this.maxCharactersPerLine = maxCharactersPerLine;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#start()
	 */
	@Override
	public void start() {
		Runnable runnable = new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				reader = new InputStreamReader(new LimitedInputStream(in, maxBytesAllowed));
				
				try {
					int c;
					StringBuilder line = new StringBuilder();
					while (true) {
						c = reader.read();
						if (c < 0) {
							// End of input.
							// See if we have a partial line.
							if (line.length() > 0 && collectedLines.size() < maxLinesAllowed) {
								collectedLines.add(line.toString());
							}
							break;
						}

						// If we've exceeded max number of lines, ignore this character.
						if (collectedLines.size() > maxLinesAllowed) {
							continue;
						}

						// Reached end of line?
						if (c == '\n') {
							collectedLines.add(line.toString());
							line = new StringBuilder();
						} else {
							// Only append the character if we haven't exceeded the max
							// number of characters per line.
							if (line.length() <= maxCharactersPerLine) {
								line.append((char) c);
							}
						}
					}
				} catch (IOException e) {
					// Ignore
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		};
		thread = new Thread(runnable);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#interrupt()
	 */
	@Override
	public void interrupt() {
		thread.interrupt();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#join()
	 */
	@Override
	public void join() throws InterruptedException {
		thread.join();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#getCollectedOutput()
	 */
	@Override
	public List<String> getCollectedOutput() {
		return Collections.unmodifiableList(collectedLines);
	}
}
