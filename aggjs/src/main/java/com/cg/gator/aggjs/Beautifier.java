package com.cg.gator.aggjs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Aravind Krishnan
 */
public class Beautifier
{
	
	  /**
	 * Constructor.
	 * @param argDestinationFile
	 */
	public Beautifier(File argDestinationFile)
	{
		super();
		this.destinationFile = argDestinationFile;
	}

	private static final String BEAUTIFY_JS = "/beautify.js";
	  
	private File destinationFile;
	  
	  
	/**
	 * 
	 */
	@SuppressWarnings("resource")
	public void beautify() {
		  
		  if(this.destinationFile == null) {
			  return;
		  }
		  OutputStream output = null;
	    	try
			{
	    		System.out.println("Beautifying " + this.destinationFile.getName());
				String input = IOUtils.toString(new FileInputStream(this.destinationFile));
				output = new BufferedOutputStream(new FileOutputStream(this.destinationFile, false));
				IOUtils.write(jsBeautify(input), output);
				System.out.println("Done without errors.\n");
				
			}
			catch (FileNotFoundException e)
			{
				
				e.printStackTrace();
				
			}
			catch (IOException e)
			{
				
				e.printStackTrace();
				
			}
	    	finally {
	            IOUtils.closeQuietly(output);
	        }
	    	
		  
	  }
	  
	  /**
		 * @param jsCode
		 * @return
		 */
		@SuppressWarnings("resource")
		public static String jsBeautify(String jsCode) {
		    Context cx = Context.enter();
		    Scriptable scope = cx.initStandardObjects();
		    InputStream resourceAsStream = BeautifyMojo.class.getResourceAsStream(BEAUTIFY_JS);
		    
		    try {
		      Reader reader = new InputStreamReader(resourceAsStream);
		      cx.evaluateReader(scope, reader, "__beautify.js", 1, null);
		      reader.close();
		    } 
		    catch (IOException e) {
		      System.out.println("Error reading " + "beautify.js");
		    }
		    finally {
	            IOUtils.closeQuietly(resourceAsStream);
	        }
		    scope.put("jsCode", scope, jsCode);
		    return (String) cx.evaluateString(scope, "js_beautify(jsCode, {indent_size: 2 })",
		        "inline", 1, null);
		  }
	
}
