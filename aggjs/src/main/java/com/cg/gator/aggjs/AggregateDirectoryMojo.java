package com.cg.gator.aggjs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal aggregate
 * 
 * @phase process-sources
 */

public class AggregateDirectoryMojo extends AbstractMojo
{
	
	/**
	 * Constructor.
	 */
	public AggregateDirectoryMojo()
	{
	}
	
	/**
     * Location of the folder.
     * @parameter expression="${watchDirectory}"
     * @required
     */
    private File watchDirectory;
    
    /**
     * Location of the destination.
     * @parameter expression="${destinationFile}"
     * @required
     */
    private File destinationFile;

    /**
    * A set of file patterns to include in the zip.
    * @parameter alias="jsIncludes"
    */
    private File[] jsIncludes;
    
    /**
     * Location of the destination js file.
     * @parameter expression="${destinationTemplateFile}"
     */
	private File destinationTemplateFile;
	
	 /**
     * Location of the templates  file.
     * @parameter expression="${templateFilesLocation}"
     */
	private File templateFilesLocation;
	
	 /**
     * Package name for the JS Templates
     * @parameter expression="${underscoreJsLocation}"
     */
	private String jsTemplatePackage;
	
	
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		if(!this.watchDirectory.exists() || !this.watchDirectory.isDirectory()) {
    		return;
    	}
    	
    	// destinationFile has to be a file(not a directory)
    	if(this.destinationFile ==null) {
    		return;
    	}
    	
        //define a folder root
        Path myDir = Paths.get(this.watchDirectory.getAbsolutePath());       
        	
        try
		{
        	new UnderscoreTemplates(this.destinationTemplateFile,this.templateFilesLocation, this.jsTemplatePackage).compile();
        	new Aggregator(myDir, this.destinationFile,this.jsIncludes).aggregate();
			new Beautifier(this.destinationFile).beautify();
		}
		catch (IOException e)
		{
			
			e.printStackTrace();
			
		}
		
	}
	

	
}
