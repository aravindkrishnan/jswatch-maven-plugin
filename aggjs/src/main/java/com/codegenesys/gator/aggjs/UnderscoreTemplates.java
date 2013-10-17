package com.codegenesys.gator.aggjs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Aravind Krishnan

 */
public class UnderscoreTemplates
{
	
	private static final String   UNDERSCORE_JS = "/underscore.js";
	private static final String   TEMPLATE_TYPE = ".html";
	
	/**
	 * Constructor.
	 * @param argUnderscoreJsLocation
	 * @param argDestinationTemplateFile
	 * @param argTemplateFilesLocation
	 * @param argJsTemplatePackage 
	 */
	public UnderscoreTemplates(File argDestinationTemplateFile,
			File argTemplateFilesLocation, String argJsTemplatePackage)
	{
		super();
		this.destinationTemplateFile = argDestinationTemplateFile;
		this.templateFilesLocation = argTemplateFilesLocation;
		this.jsTemplatePackage     = argJsTemplatePackage;
	}
	
	private File destinationTemplateFile;
	private File templateFilesLocation;
	private String jsTemplatePackage;
	
	
	
	
	/**
	 * 
	 */
	@SuppressWarnings("resource")
	public void compile() {
			
		
		if(Utils.isFile(this.templateFilesLocation))        { return; }	
		if(!Utils.isFile(this.destinationTemplateFile))     { return; }	
		if(Utils.stringNullOrEmpty(this.jsTemplatePackage)) { return; }
		
		
		System.out.println("\nCompiling underscore templates...");
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();
		InputStream resourceAsStream = null;
		OutputStream output = null;
		 
		try {
		    	resourceAsStream = UnderscoreTemplates.class.getResourceAsStream(UNDERSCORE_JS);
		    	output = Utils.createAppendableStream(this.destinationTemplateFile);
		    	 
		        Reader reader = new InputStreamReader(resourceAsStream);
			    cx.evaluateReader(scope, reader, "_underscore.js", 1, null);
			    reader.close();
			    List<File> templateFiles = Utils.fileWalker(Paths.get(this.templateFilesLocation.getAbsolutePath()));
			  
			    for(File templateFile : templateFiles){
			    	
			    	if(!fileisTemplateType(templateFile.getAbsolutePath())) {
			    		continue;
			    	}
			    		
			    	InputStream tempStream = new FileInputStream(templateFile);
			    	String tempSource = IOUtils.toString(tempStream);
			    	
			    	scope.put("htmlCode", scope, tempSource);
				    String templ =  (String) cx.evaluateString(scope, "_.template(htmlCode).source.replace(/[\\n\\t]/g, '');",
				        "inline", 1, null);
				    
				    appendTemplate(output, templ, templateFile.getName());
			         
			        IOUtils.closeQuietly(tempStream);
			    }
		  }
		    
		  catch (IOException e) {
			      System.out.println("Error reading " + "underscore.js");
		   }
		   finally {
	            IOUtils.closeQuietly(resourceAsStream);
	            IOUtils.closeQuietly(output);
	       }
		    
		 System.out.println("Done without errors."); 
	}
	
    
	private void appendTemplate(OutputStream output, String data, String fileName)
            throws IOException 
   {
	  String  fdata = this.jsTemplatePackage+ "['" + cleanFileName(fileName) + "'] = " + data + ";\n";
      IOUtils.copy(new StringReader(fdata), output);
       
   }
	
   private static String cleanFileName(String fileName) {	
	   
	   return FilenameUtils.removeExtension(fileName);
   }
	
	private static boolean fileisTemplateType(String file) {
		return file.endsWith(TEMPLATE_TYPE);
	}
	
	
	
	
}
