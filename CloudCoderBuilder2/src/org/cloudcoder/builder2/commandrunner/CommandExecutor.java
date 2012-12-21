// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.commandrunner;

import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.CommandLimit;
import org.cloudcoder.builder2.model.CommandResult;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.process.LimitedProcessRunner;
import org.cloudcoder.builder2.process.ProcessRunner;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute a {@link Command} to produce a {@link CommandResult}.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class CommandExecutor implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	private static final int MAX_TEST_EXECUTOR_JOIN_ATTEMPTS = 10;

	private Command command;
	private CommandInput commandInput;
	private CommandExecutionPreferences prefs;
	
	private Thread thread;
	private CommandResult commandResult;

	/**
	 * Number of milliseconds between polls to see if a test case
	 * process has completed.
	 */
	public static final int POLL_INTERVAL_IN_MILLIS = 500;

	/**
	 * Maximum number of seconds (wall time) to allow a command process to run
	 * by default.
	 */
	public static final int DEFAULT_MAX_TIME_IN_SECONDS = 8;

	/**
	 * Constructor.
	 * 
	 * @param command the {@link Command} to execute
	 * @param commandInput the {@link CommandInput} to use to provide input to the command
	 */
	public CommandExecutor(Command command, CommandInput commandInput) {
		this.command = command;
		this.commandInput = commandInput;
	}
	
	/**
	 * Set {@link CommandExecutionPreferences} (process limits).
	 * 
	 * @param prefs the {@link CommandExecutionPreferences} to set
	 */
	public void setPrefs(CommandExecutionPreferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * @return the testCase
	 */
	public CommandInput getTestCase() {
		return commandInput;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// FIXME: allow use of a SECCOMP sandbox
		
		int maxWaitTimeSec;
		
		ProcessRunner processRunner;
		if (prefs != null) {
			LimitedProcessRunner processRunner_ = new LimitedProcessRunner();
			processRunner_.setPreferences(prefs);
			processRunner = processRunner_;
			maxWaitTimeSec = prefs.getLimit(CommandLimit.CPU_TIME_SEC) * 2;
		} else {
			processRunner = new ProcessRunner();
			maxWaitTimeSec = DEFAULT_MAX_TIME_IN_SECONDS;
		}

		processRunner.setStdin(commandInput.getInput());

		String[] cmd = ArrayUtil.toArray(command.getArgs(), String.class);
		processRunner.runAsynchronous(command.getDir(), cmd);

		int elapsed = 0;
		while (processRunner.isRunning() && elapsed < maxWaitTimeSec * 1000) {
			try {
				Thread.sleep(CommandExecutor.POLL_INTERVAL_IN_MILLIS);
			} catch (InterruptedException e) {
				// can't happen
			}

			elapsed += CommandExecutor.POLL_INTERVAL_IN_MILLIS;
		}

		if (processRunner.isRunning()) {
			// timed out!
			processRunner.killProcess();
			commandResult = new CommandResult(ProcessStatus.TIMED_OUT, processRunner.getStatusMessage());
		} else {
			// Either completed normally or killed by signal
			commandResult = new CommandResult(
					processRunner.getStatus(),
					processRunner.getStatusMessage(),
					processRunner.getExitCode(),
					processRunner.getStdoutAsList(),
					processRunner.getStderrAsList());
		}
	}

	/**
	 * Start executing the {@link Command}.
	 */
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Wait for the {@link Command} to complete.
	 */
	public void join() {
		boolean done = false;
		int numAttempts = 0;
		while (!done && numAttempts < MAX_TEST_EXECUTOR_JOIN_ATTEMPTS) {
			try {
				thread.join();
				done = true;
			} catch (InterruptedException e) {
				logger.error("test executor interrupted unexpectedly");
				numAttempts++;
			}
		}
		if (!done) {
			logger.error(
					"could not join test executor after {} attempts - giving up",
					MAX_TEST_EXECUTOR_JOIN_ATTEMPTS);
			commandResult = new CommandResult(ProcessStatus.COULD_NOT_START, "Command executor did not finish");
		}
	}
	
	/**
	 * @return the {@link CommandResult}
	 */
	public CommandResult getCommandResult() {
		return commandResult;
	}
}