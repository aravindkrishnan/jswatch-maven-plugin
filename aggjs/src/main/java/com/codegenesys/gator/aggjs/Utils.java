package com.codegenesys.gator.aggjs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aravind Krishnan
 */
public class Utils
{
	
	/**
	 * @param file
	 * @return
	 */
	public static boolean isFile(File file) {
    	return file != null && file.isFile();
    }
	
	/**
	 * @param file
	 * @return
	 */
	public static boolean isDirectory(File file) {
    	return file != null && file.isDirectory();
    }
	
	/**
	 * @param str
	 * @return
	 */
	public static boolean stringNullOrEmpty(String str) {
		return str == null || str.length() <= 0;
	}
	
	/**
	 * @param destination
	 * @return
	 * @throws FileNotFoundException
	 */
	public static BufferedOutputStream createAppendableStream(File destination) throws FileNotFoundException 
    {
	    return new BufferedOutputStream(new FileOutputStream(destination, false));
    }
	
	
	/**
	 * @param root
	 * @return
	 * @throws IOException
	 */
	public static List<File> fileWalker(Path root) throws IOException
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
	
}
