/**
 * 
 */
package filz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * while this class, itself, represents a single path-action, 
 * the static methods implement operations on the whole collection
 *  - getConfig loads the array
 *  - check_path gets info on one ...
 *  
 * @author gparks1
 *
 * the current implementation is a kludgey two-layer facade :
 *   + the ArrayList is the core loaded, 
 *   + the HashMap is the quick-access (constant time, or log_2 n ...), cached, maybe incomplete, data
 * (I could re-implement this to put 'everything' in the HashMap at load time  
 */
public class PathAction implements Comparable<String> {
	protected String filename;  // *NOT* canonical name, but the "default" string value ...
	protected boolean bSkip;

	protected PathAction(String n, boolean sk) {
		filename = n;
		bSkip = sk;
	}
	
	@Override
	public int compareTo(String s) {
		return filename.compareTo(s);
	}
	
	/**
	 * the following (two) member vars and the check_path method implement the "exposed" operations of this
	 */
	// https://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html
	protected static ArrayList<PathAction> pas = PathAction.getConfig("actions.json");
	
	private static java.util.HashMap<String, Boolean> mSkips = new java.util.HashMap<String, Boolean>();

	/**
	 * 
	 * @param n
	 * @return boolean: should this (sub-directory/path) be skipped 
	 */	
	public static boolean check_path(String n)
	{
		// System.out.println("\t   DEBUG: pas is " + pas.size() + " element(s)");
		boolean retval = false;  // default to "don't skip"
		if (mSkips.containsKey(n)) {
			retval = mSkips.get(n);
		} else {
			Iterator<PathAction> i = pas.iterator();
			boolean bCont = i.hasNext();
			while (bCont) {
				PathAction x = i.next();
				if (x.compareTo(n) == 0) {
					retval = x.bSkip;
					bCont = false;
				} else {
					bCont = i.hasNext();
				}
				// "cache" the result, so next lookup is faster!
				mSkips.put(n, retval);
			}
		}
		
		return retval;
	}

	

	/**
	 * loads the array of path actions from the config 
	 * 
	 * @param fn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<PathAction> getConfig(String fn) 
	{
		ArrayList<PathAction> retval = new ArrayList<PathAction>();
		ObjectMapper mapper = new ObjectMapper();

		/* 
		 * this worked for an empty list (i.e. "[]")
		 */
		try {
			List<LinkedHashMap<String, Object>> lm = mapper.readValue(new File(fn), java.util.List.class);
			for (LinkedHashMap<String, Object> lhm: lm) {
				// System.out.println("     PathAction.getConfig: an(other) item in the list");
				// Set<String> ks = lhm.keySet();
				// for(String k: ks) {
				// 	Object v = lhm.get(k);
				// 	System.out.println("\t PathAction.getConfig: key " + k + " = " + v);
				// }
				String name = (String)(lhm.get("name"));
				boolean sk = true;
				String s = (String)(lhm.get("skip"));
				if (s != null) {
					if (!s.substring(0,1).toLowerCase().equals("t") 
							&&  !s.substring(0,1).toLowerCase().equals("y") 
							&&  !s.equals("1")) {
						// System.err.println("    PathAction.getConfig 'skip' for " + name + " was NOT true");
						sk = false;
					}
				} else {
					System.err.println("\t ** PathAction: `get` skip was NULL for " + name);
				}
				PathAction pa = new PathAction(name, sk);
				retval.add(pa);
			}
			
		} catch (JsonParseException e) {
			System.err.println(" PathAction.getConfig: JSON parse exception: " + e.getLocalizedMessage());
		} catch (JsonMappingException e) {
			System.err.println(" PathAction.getConfig: JSON mapping exc.: " + e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("  PathAction.getConfig: done");
		return retval;
	}
	
}
