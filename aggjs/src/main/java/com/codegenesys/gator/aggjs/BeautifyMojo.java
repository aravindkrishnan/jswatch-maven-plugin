package com.codegenesys.gator.aggjs;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal beautify
 * 
 * @phase process-sources
 */

public class BeautifyMojo extends AbstractMojo
{

	 /**
     * Location of the destination.
     * @parameter expression="${destinationFile}"
     * @required
     */
    private File destinationFile;
    
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		// destinationFile has to be a file(not a directory)
    	if(Utils.isFile(this.destinationFile)) { return; }
    	
    	new Beautifier(this.destinationFile).beautify();
	}
	
	
	
}
