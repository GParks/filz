package filz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
// import java.nio.file.Files;
import java.nio.file.attribute.UserPrincipal;

import java.util.ArrayDeque;
import java.util.ArrayList;
// import java.util.Iterator;

// https://github.com/FasterXML/jackson
// https://github.com/FasterXML/jackson-databind/

// https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/

// import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
// import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.annotation.JsonView;
// import com.fasterxml.jackson.*;
// import com.fasterxml.jackson.annotation.*;

class PathCount  {
	protected String name;
	protected int count;
	public PathCount(String n, int c) {
		name = n;
		count = c;
	}
}

public class Directory {

	// protected static String[] ss = {"Greg", "was", "here"};
	
	protected Path root_or_parent(Path p) {
		Path retval = null;
		
		System.out.println("  Path = " + p.toString());
		Path r = p.getRoot();
		try {
			if (null == r) {
				System.err.println(" `getRoot` returned null");
				Path p1 = p.getParent();
				while (null != p1) {
					retval = p1;
					System.out.println("  parent =" + retval);
					p = p1;
					p1 = p.getParent();
				}
			} else {
				System.out.println("  root file canonical path:" + r.toFile().getCanonicalPath() );
				retval = r;
			}
		} catch (IOException i) {
			System.err.println("Oops: " + i.getLocalizedMessage());
			System.err.println("  (" + i.toString() + ")");
		}
		return retval;
	}
	
	protected Path p_fs_root = null;
	/**
	 * fDirs = "open list" of directories
	 */
	protected ArrayDeque<File> fDirs = new ArrayDeque<File>();
	
	protected ArrayList<PathCount> pcs = new ArrayList<PathCount>();
	
	protected void scan_filesystem() {
		java.nio.file.FileSystem fsDef = java.nio.file.FileSystems.getDefault();
		for(java.nio.file.FileStore fs: fsDef.getFileStores()) {
			System.out.println("  a FileStore in the default FileSystem is " + fs.name() + ", a(n) " + fs.type() + " type of fs");
		}
		System.out.println();
		
		for(Path p: fsDef.getRootDirectories()) {
			assert(null == p_fs_root);
			p_fs_root = p;
			System.out.println("  a root directory: " + p);
		}
		
		fDirs.addLast(p_fs_root.toFile());  // `add` equiv. to `addLast`, but let's be explicit
		
	}
	
	private boolean add_path(String cp, int l) 
	{
		boolean retval = true;
		
		// I was going to check for dups here -- I may still do so, but maybe don't need to
		pcs.add(new PathCount(cp, l));
		
		return retval;
	}
	
	
	// I could (easily) make this an iterator
	protected boolean subdirs() {
		return 
			subdirs(-1);
	}
	
	protected static final boolean bSkipUP = true;
	
	protected boolean subdirs(int limit) {
		boolean retval = false;
		
		if (!fDirs.isEmpty()) {
			File n = fDirs.pop();
			String[] ls = n.list();
			File[] fs = n.listFiles();

			String canonical_of_n = null;
			 try {
				 canonical_of_n = n.getCanonicalPath();
			} catch (IOException e) {
				System.err.println("\t  subdirs: IO exc. getting canonical path:");
				e.printStackTrace();
			}

			if (null == ls) {
				if (null != fs) {
					// System.err.println("\t  list [String] and l. of files (of " + n + ") both null");					
				// } else {
					System.err.println("\t  list (of " + n + ") is null, but `listFiles` was NOT");
				}
				if (null != canonical_of_n)
					add_path(canonical_of_n, -1);
				else 
					System.err.println("\t  not adding " + n + " (with no length) due to prev. IOException");
				// continue, anyway
				retval = true;
			} else if (null == fs) {
				System.err.println("\t  ** list of files (of " + n + ") is null ** ");
			} else {
				assert (ls.length == fs.length);
				// the following works "breadth-first"
				if (null != canonical_of_n)
					add_path(canonical_of_n, fs.length);
				else
					System.err.println("\t  not adding " + n + ", " + fs.length + " due to prev. IOException");
				
				// 
				// loop over all files in this directory
				// 
				for(File f: fs) {
					Path p = f.toPath();
					boolean bAddSubs = !PathAction.check_path(f.toString());
					System.out.println("\t DEBUG: bAddSubs for " + f + " = " + bAddSubs);
					String cp_of_f = null;
					try {
						cp_of_f = f.getCanonicalPath();
					} catch (IOException io) {
						System.err.println("    IOException getting canonical path for " + f);
					}
					String sSymLink = "";
					if ( java.nio.file.Files.isSymbolicLink(p) ) {
						sSymLink = "\n\t nio reports this is sym link!";
					} else if (!bSkipUP) {
						sSymLink = "\n\t";
					}
					
					String sUserPrinc = null;
					if (bSkipUP) {
						sUserPrinc = "";
					} else {
						sUserPrinc = "NO USER PRICIPAL";
						try {
							UserPrincipal up = java.nio.file.Files.getOwner(p, java.nio.file.LinkOption.NOFOLLOW_LINKS);
							sUserPrinc = "\t user principal: name = \""  + up + "\"";
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}					 
					
					boolean bIsDir = f.isDirectory();
					boolean bNormal = f.isFile();
					assert ( (bIsDir || bNormal) && !(bIsDir && bNormal) );
					if (bIsDir) {
						// experience has shown that isDir  --> !isFile 
						System.out.println("  Directory: " + f + "; canonical name = \"" + cp_of_f  + "\", abs path = \"" + f.getAbsolutePath() + "\"" +
								"; hidden? " + f.isHidden()  + sSymLink + sUserPrinc );
						if (bAddSubs) {
							// System.out.println("    adding directory " + f);
							fDirs.add(f);							
						}
						
					} else {
						System.out.println("  File: " + f + "; canonical name = \"" + cp_of_f  + "\", abs path = \"" + f.getAbsolutePath() + "\"" +
								"; hidden? " + f.isHidden()  + sSymLink + sUserPrinc );
					}					
					
				}
				System.out.println("  - now " + fDirs.size() + " sub-dir(s)");
				retval = true;
				if (limit >= 0) {
					if (fDirs.size() > limit)
						retval = false;
				}
			}
		} else {
			System.out.println("\t subdirs: fDirs isEmpty - done!");
		}
		return retval;
	}
	
//	protected static void test_json_writer() {
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			mapper.writeValue(new File("test.json"), ss);
//			for(String s: ss) {
//				System.out.println("  test_json_writer: string = \"" + s + "\"");
//			}
//		} catch (JsonGenerationException e) {
//			System.err.println("test_json_writer: JSON gen. exception: " + e.getLocalizedMessage());
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			System.err.println("test_json_writer: JSON mapping exc.: " + e);
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("test_json_writer: IO exc." + e.getMessage());
//			e.printStackTrace();
//		} catch (Exception e) {
//			System.err.println("test_json_writer: Exception " + e);
//		}
//		
//	}

	protected static void test_path_act_compare() {
		PathAction.check_path("/Volumes/Macintosh HD");
	}
	
	@SuppressWarnings("rawtypes")
	protected static void test_json_reader() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			java.util.List lm = mapper.readValue(new File("test_in.json"), java.util.List.class);
			System.out.println("lm is a(n) " + lm.getClass());  // java.util.ArrayList
			System.out.println("  test_json_reader: length of 'l'ist of 'm'yobjs = " + lm.size());
			System.out.println("  test_json_reader: first item is (a) " + lm.get(0).getClass().getCanonicalName());
			java.util.LinkedHashMap lhm = (java.util.LinkedHashMap)(lm.get(0));
			java.util.Set ks = lhm.keySet();
			for(Object o: ks) {
				System.out.println("\t  test_json_reader: (a) key: " + o.toString()); 
				
			}
		} catch (JsonParseException e) {
			System.err.println("test_json_reader: JSON parse exception: " + e.getLocalizedMessage());
			// e.printStackTrace();
		} catch (JsonMappingException e) {
			// "test_json_reader: JSON mapping exc.: Cannot deserialize instance of `java.util.LinkedHashMap<java.lang.Object,java.lang.Object>` out of START_ARRAY token"
			System.err.println("test_json_reader: JSON mapping exc.: " + e.getLocalizedMessage());
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException cce) {
			System.err.println("test_json_reader: class cast exception: " );
			cce.printStackTrace();
			
		}
	}
	
	/**
	 * main func.
	 * first, it fails to get root or parent (though I'm leaving the code here);
	 * then:  
	 * `scan_filesystem`
	 * `while (dir.subdirs(limit))`
	 * 
	 * @param args - 1st cmd line arg. = count  (   0     --> test; 
	 *                                           negative --> no limit)
	 */
	public static void main(String[] args) {

		int limit = 50000;
		
		if (args.length > 0) {
			limit = Integer.parseInt(args[0]);
			System.out.println("first arg is " + args[0] + " (" + limit + ")");
		} // else {
		// 	System.out.println("\t zero args");
		// }
		
		if (0 == limit) {
			test_path_act_compare();
		} else {		
			Directory dir = new Directory();
			File f = new File(".");
			if (f.exists()) {
				System.out.println(" file '.' exists");
				System.out.println("  abs path = " + f.getAbsolutePath());
				Path top = dir.root_or_parent(f.toPath());
				System.out.println("root or parent is: " + top);
			} else {
				System.err.println("file '.' does NOT exist");
			}
			
			// 
			// heart of the code
			//
			dir.scan_filesystem();

			while (dir.subdirs(limit)) {
			
			}
			
			int total = 0;
			for (PathCount pc: dir.pcs) {
				System.out.println("  path \"" + pc.name + "\", \t  count = " + pc.count);
				total += pc.count;
			}
			System.out.println("  total = " + total);
		}
	}

}
