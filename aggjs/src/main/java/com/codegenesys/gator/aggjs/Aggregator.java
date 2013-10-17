package com.codegenesys.gator.aggjs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * @author Aravind Krishnan
 */
public class Aggregator
{
	private final Path          rootFolder;
	private final File          destinationFile;
	private final File[]        includeFiles;
	
	private static final String        JS_TYPE = ".js";
	/**
	 * Constructor.
	 * @param rootFolderr 
	 * @param destinationnFile 
	 * @param includeFiless 
	
	 */
	public Aggregator(Path rootFolderr, File destinationnFile,File[] includeFiless)
	{
		this.rootFolder          =   rootFolderr;
		this.destinationFile     =   destinationnFile;
		this.includeFiles        =   includeFiless;
	}
	
	/**
	 * @throws IOException
	 */
	public void aggregate() throws IOException{
		
		  System.out.println("Aggregating...");
		 
		  final List<File> files = new ArrayList<File>();
		  
		  if(this.includeFiles.length == 0) {
			  mergeAll(files, fileWalker(this.rootFolder));
		  }
		  else {
			  for (File source : this.includeFiles) {
				  if(source == null || !source.exists()) {
		            	 continue;
	              }
				  if(source.isDirectory()) {
					  mergeAll(files, fileWalker(Paths.get(source.getAbsolutePath())));
				  }
				  else {
					  merge(files, source);
				  }
			  }
		  }
		  weedNonJSFiles(files);
		  joinFiles(this.destinationFile, files);
		  // printAllFiles(files);
		  System.out.println("Done without errors.\n");
	}
	
	private static List<File> fileWalker(Path root) throws IOException
	{
		final List<File> files = new ArrayList<File>();
		 Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
	            @Override
	            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) 
	            		throws IOException 
	            {
	            	files.add(file.toFile());
	            	return FileVisitResult.CONTINUE;
	            	
	            }
	        });
		 return files;
	}
	
	private static void mergeAll(List<File> finalFiles, List<File> files){
		int i=0;
		List<Integer> removables = new ArrayList<Integer>();
		for(File finalFile: finalFiles) {
			
			for(File file: files) {
				if(fileAlreadyAdded(finalFile.getAbsolutePath(),file.getAbsolutePath())) {
					removables.add(i);
				}	
			}
		i++;	
		}
		removeFromList(finalFiles, removables);
		
		finalFiles.addAll(files);
		
	}
	
	private static void merge(List<File> finalFiles, File file){
		int i=0;
		List<Integer> removables = new ArrayList<Integer>();
		for(File finalFile: finalFiles) {
				if(fileAlreadyAdded(finalFile.getAbsolutePath(),file.getAbsolutePath())) {
					removables.add(i);
				}
		i++;	
		}
		removeFromList(finalFiles, removables);
		
		if(fileisJSType(file.getAbsolutePath())){
			finalFiles.add(file);
		}
	}
	
	private static void removeFromList(List<File> files, List<Integer> removables) {
		 Collections.sort(removables,Collections.reverseOrder());
		 for(Integer r : removables) {
		  files.remove(r.intValue());
		 }
	}
	
	private static boolean fileAlreadyAdded(String file1, String file2) {
		return file1.equalsIgnoreCase(file2);
	}
	
	private static boolean fileisJSType(String file) {
		return file.endsWith(JS_TYPE);
	}
	
	private static void weedNonJSFiles(List<File> files) {
		 int i=0;
		  List<Integer> removables = new ArrayList<Integer>();
		  for(File file: files) {
			  	if(!fileisJSType(file.getAbsolutePath())){
			  		removables.add(i);
				}
			  i++;	
		  }
		  removeFromList(files, removables);
			
	}
	
	@SuppressWarnings("unused")
	private static void printAllFiles(List<File> files) {
		for(File file: files) {
		  System.out.println(file.getAbsolutePath());
	  }
	}
	
	@SuppressWarnings("resource")
	private static void appendFile(OutputStream output, File source)
	            throws IOException 
	{
	        InputStream input = null;
	        try {
	            input = new BufferedInputStream(new FileInputStream(source));
	            IOUtils.copy(input, output);
	        } finally {
	        	IOUtils.write("\n", output);
	            IOUtils.closeQuietly(input);
	        }
    }
    
    /**
     * @param destination
     * @param sources
     * @throws IOException
     */
    @SuppressWarnings("resource")
	public static void joinFiles(File destination, List<File> sources)
            throws IOException {
        OutputStream output = null;
        System.out.println("Creating "+ destination.getName());
        try {
            output = Utils.createAppendableStream(destination);
            for (File source : sources) {
            	appendFile(output, source);
            }
        } finally {
            IOUtils.closeQuietly(output);
        }
    }
	
	
}
