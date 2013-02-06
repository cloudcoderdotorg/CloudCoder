package org.cloudcoder.builder2.commandrunner;

import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandLimit;
import org.cloudcoder.builder2.model.IBuildStep;

/**
 * Create a {@link CommandExecutionPreferences} object to sandbox
 * executed commands.  This is useful for sandboxing commands
 * to run C and C++ test executables.
 * 
 * @author David Hovemeyer
 */
public class CreateLimitedCommandExecutionPreferencesBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		CommandExecutionPreferences prefs = new CommandExecutionPreferences();
		
		prefs.setLimit(CommandLimit.CPU_TIME_SEC, 5);
		prefs.setLimit(CommandLimit.FILE_SIZE_KB, 0);
		prefs.setLimit(CommandLimit.OUTPUT_LINE_MAX_CHARS, 400);
		prefs.setLimit(CommandLimit.OUTPUT_MAX_BYTES, 4000);
		prefs.setLimit(CommandLimit.OUTPUT_MAX_LINES, 40);
		prefs.setLimit(CommandLimit.PROCESSES, 0);
		prefs.setLimit(CommandLimit.STACK_SIZE_KB, 256);
		prefs.setLimit(CommandLimit.VM_SIZE_KB, 32768);
		
		submission.addArtifact(prefs);
	}

}
