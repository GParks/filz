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
// import java.util.Set;

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
	protected String full_path;  // *NOT* canonical name, but the "default" string value ...
	protected String name_pattern;
	protected boolean bSkip;

	protected PathAction(String p, String m, boolean sk) {
		full_path = p;
		name_pattern = m;
		bSkip = sk;
	}
	
	@Override
	public int compareTo(String s) {
		if (null == full_path)
			return -9;
		return full_path.compareTo(s);
	}

	/**
	 * public utility method to parse booleans "my way"
	 * (the below code does not use this, because I want to [manually] check null values there,
	 *   and report that situation)
	 * @param s
	 * @return T/F (default to false)
	 */
	public static boolean string_to_bool(String s) {
		boolean retval = false;
		if (null != s && s.length() > 0) {
			if (s.substring(0,1).toLowerCase().equals("t") 
					||  s.substring(0,1).toLowerCase().equals("y") 
					||  s.equals("1")) {
				retval = true;
			}
		}		
		return retval;
	}

	/**
	 * the following (two) member vars and the check_path method implement the "exposed" operations of this
	 */
	// https://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html
	protected static ArrayList<PathAction> pas = PathAction.getConfig("actions.json");
	
	private static java.util.HashMap<String, Boolean> mSkips = new java.util.HashMap<String, Boolean>();

	protected static final int N_DBG_LVL = 4;
	
	/**
	 * 
	 * @param n
	 * @return boolean: should this (sub-directory/path) be skipped 
	 */	
	public static boolean check_path(String n)
	{
		if (N_DBG_LVL > 8) {
			System.out.println("\t    check_path: DEBUG: pas is " + pas.size() + " element(s)");
		}
		boolean retval = false;  // default to "don't skip"
		if (mSkips.containsKey(n)) {
			retval = mSkips.get(n);
			if (N_DBG_LVL > 6) {
				System.out.println("\t    check_path: found " + n + ", returning " + retval);
			}
		} else {
			Iterator<PathAction> i = pas.iterator();
			boolean bCont = i.hasNext();
			while (bCont) {
				PathAction x = i.next();
				if (x.compareTo(n) == 0) {
					retval = x.bSkip;
					if (N_DBG_LVL > 6) {
						System.out.println("\t    check_path: compare matched, " + n + " = " + x.full_path +  
											", returning " + retval);
					}
					bCont = false;
				} else {
					bCont = i.hasNext();
				}
				// "cache" the result, so next lookup is faster!
				mSkips.put(n, retval);
			}
			if (N_DBG_LVL > 6) {
				System.out.println("\t      check_path: (adding value for, " + n + ", r. " + retval + ")");
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

		if (N_DBG_LVL > 3) {
			System.out.println("\t    PathAction.getConfig starting");
		}
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
				String path = (String)(lhm.get("path"));
				String match = (String)(lhm.get("name"));  // may be wildcard
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
					System.err.println("\t ** PathAction: `get` skip was NULL for " + path);
				}
				PathAction pa = new PathAction(path, match, sk);
				retval.add(pa);
			}
			
		} catch (JsonParseException e) {
			System.err.println(" PathAction.getConfig: JSON parse exception: " + e.getLocalizedMessage());
			System.err.println("    ** NO 'actions.json' config loaded ** ");
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
