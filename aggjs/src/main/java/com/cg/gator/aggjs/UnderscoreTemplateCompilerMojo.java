package com.cg.gator.aggjs;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Goal which touches a timestamp file.
 *
 * @goal underscoretemplate
 * 
 * @phase process-sources
 */

public class UnderscoreTemplateCompilerMojo extends AbstractMojo
{
	
	
	 /**
     * Location of the destination js file.
     * @parameter expression="${destinationTemplateFile}"
     * @required
     */
	private File destinationTemplateFile;
	
	 /**
     * Location of the templates  file.
     * @parameter expression="${templateFilesLocation}"
     * @required
     */
	private File templateFilesLocation;
	
	 /**
     * Package name for the JS Templates
     * @parameter expression="${underscoreJsLocation}"
     * @required
     */
	private String jsTemplatePackage;
	
		/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		
		if(this.templateFilesLocation == null || this.templateFilesLocation.isFile()) {
    		return;
    	}
		
		if(this.destinationTemplateFile ==null || !this.destinationTemplateFile.isFile()) {
    		return;
    	}
		if(this.jsTemplatePackage ==null || this.jsTemplatePackage.length() <= 0) {
    		return;
    	}
		
		new UnderscoreTemplates(this.destinationTemplateFile,
				this.templateFilesLocation, this.jsTemplatePackage).compile();
	}
	
	
	
	
	
}
