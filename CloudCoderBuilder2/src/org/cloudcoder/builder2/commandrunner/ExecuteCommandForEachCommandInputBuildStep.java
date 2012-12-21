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

package org.cloudcoder.builder2.commandrunner;

import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.CommandResult;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;

/**
 * An {@link IBuildStep} to execute a {@link Command} for each {@link CommandInput}
 * and save the result of each execution as a {@link CommandResult}.
 * An array of {@link CommandResult}s is added to the submission
 * as an artifact. 
 * 
 * @author David Hovemeyer
 */
public class ExecuteCommandForEachCommandInputBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		// Get the Commands
		Command[] commandList = submission.getArtifact(Command[].class);
		if (commandList == null) {
			throw new InternalBuilderException(this.getClass(),	"No Command");
		}
		
		// Get CommandInputs
		CommandInput[] commandInputList = submission.getArtifact(CommandInput[].class);
		if (commandInputList == null) {
			throw new InternalBuilderException(this.getClass(), "No CommandInput list");
		}
		
		// See if there is a CommandExecutionPreferences
		CommandExecutionPreferences prefs = submission.getArtifact(CommandExecutionPreferences.class);
		
		// Create and start a CommandExecutor for each CommandInput
		CommandExecutor[] commandExecutorList = new CommandExecutor[commandInputList.length];
		for (int i = 0; i < commandInputList.length; i++) {
			commandExecutorList[i] = new CommandExecutor(commandList[i], commandInputList[i]);
			if (prefs != null) {
				commandExecutorList[i].setPrefs(prefs);
			}
			commandExecutorList[i].start();
		}
		
		// Wait for the CommandExecutors to complete
		for (CommandExecutor commandExecutor : commandExecutorList) {
			commandExecutor.join();
		}
		
		// Create array of CommandResults
		CommandResult[] commandResultList = new CommandResult[commandInputList.length];
		for (int i = 0; i < commandInputList.length; i++) {
			commandResultList[i] = commandExecutorList[i].getCommandResult();
		}
		submission.addArtifact(commandResultList);
	}

}
