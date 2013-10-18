package com.codegenesys.gator.aggjs;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal watch
 * 
 * @phase process-sources
 */
public class WatchDirectoryMojo extends AbstractMojo
{
	
	/**
	 * Constructor.
	 * @param argWatcher
	 * @param argWatchDirectory
	 * @throws IOException 
	 */
	public WatchDirectoryMojo() throws IOException
	{
		this.watcher =  FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();
		this.jsIncludes = new File[0];
		this.jsExcludes = new File[0];
	}

	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	
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
    * 
    * @parameter alias="jsIncludes"
    */
    private File[] jsIncludes;
    
    
    /**
     * 
     * @parameter alias="jsExcludes"
     */
     private File[] jsExcludes;
     
     
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

   
    
    public void execute() throws MojoExecutionException
    {
    	System.out.println("\n Watching: "+ this.watchDirectory.getAbsolutePath() +"\n"); 
    	System.out.println("\n Waiting... \n");
    	
    	if(!Utils.isDirectory(this.watchDirectory)) { return;  }
    	if(!Utils.isFile(this.destinationFile))     { return;  }
    	
        //define a folder root
        Path myDir = Paths.get(this.watchDirectory.getAbsolutePath());       

        try {
           registerAll(myDir);	
           boolean resetAll = false;
           while(true) {
        	   
	           WatchKey watckKey = this.watcher.take();
	
	           List<WatchEvent<?>> events = watckKey.pollEvents();
	           for (WatchEvent<?> event : events) {
	        	   
	        	    WatchEvent<Path> ev = cast(event);
	                Path name = ev.context();
	                Path child = myDir.resolve(name);
	                
	               // if directory is created, and watching recursively, then
	                // register it and its sub-directories
	                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
	                    System.out.println("Created " + name.toString());
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        	resetAll = true;
                        	break;
                        }
	                    
	                }
	                else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
	                	System.out.println("Deleted " + name.toString());
	                	this.onDelete(myDir);
	                }
	                else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
	                	// aggregate only if change is not in destination file.
                    	if(!isJsOrTemplatesFile(name)) {
                    		System.out.println("Modified " + name.toString());
		                    
		                    if(!Files.isDirectory(child, NOFOLLOW_LINKS)) {
		                      	new UnderscoreTemplates(this.destinationTemplateFile,this.templateFilesLocation, this.jsTemplatePackage).compile();
		                        new Aggregator(myDir, this.destinationFile,this.jsIncludes).aggregate();
	                    		System.out.println("\n Waiting... \n");
		                    }
                    	}
	                }
	            }
	           
	           boolean valid = watckKey.reset();
	            if (!valid) {
	                this.keys.remove(watckKey);

	                // all directories are inaccessible
	                if (this.keys.isEmpty()) {
	                    break;
	                }
	            }
	            if(resetAll) {
	            	watckKey.cancel();
	            	this.keys.clear();
	            	registerAll(myDir);	
	            }
	            	
           
           }
           
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    	
    	
       
    }
    
    
    
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    
    private void onDelete(Path dir) throws IOException {
    	new Aggregator(dir, this.destinationFile,this.jsIncludes).aggregate();
		System.out.println("\n Waiting... \n");
    	
    }
    
    
    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    
    private void registerAll(final Path start) throws IOException {
    	// register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
            	if(!isExcluded(dir))
        			register(dir);
                return FileVisitResult.CONTINUE;
            }
          
        });
    }
    
    private boolean isJsOrTemplatesFile(Path name) {
     	int isDestFile = name.toString().trim().lastIndexOf(this.destinationFile.getName());
     	int isTemplFile =name.toString().trim().lastIndexOf(this.destinationTemplateFile.getName());
     	
     	return isDestFile >=0  || isTemplFile >= 0;
    }
    
    
    private boolean isExcluded(Path file) {
    	for(File jsExclude: this.jsExcludes) {
    		if(file.toFile().getAbsolutePath().equalsIgnoreCase(jsExclude.getAbsolutePath()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isExcluded(File file) {
    	for(File jsExclude: this.jsExcludes) {
    		if(file.getAbsolutePath().equalsIgnoreCase(jsExclude.getAbsolutePath()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        this.keys.put(key, dir);
    }


}
