/**
 * 
 */
package filz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gparks1
 *
 */
public class PathAction implements Comparable<String> {
	protected String filename;  // *NOT* canonical name, but the "default" string value ...
	protected boolean bSkip;

	public PathAction(String n, boolean sk) {
		filename = n;
		bSkip = sk;
	}
	
	@Override
	public int compareTo(String s) {
		return filename.compareTo(s);
	}
	
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
				System.out.println("     PathAction.getConfig: an(other) item in the list");
				Set<String> ks = lhm.keySet();
				for(String k: ks) {
					Object v = lhm.get(k);
					System.out.println("\t PathAction.getConfig: key " + k + " = " + v);
				}
				String name = (String)(lhm.get("name"));
				boolean sk = true;
				String s = (String)(lhm.get("skip"));
				if (s != null) {
					if (!s.substring(0,1).toLowerCase().equals("t") 
							&&  !s.substring(0,1).toLowerCase().equals("y") 
							&&  !s.equals("1")) {
						System.err.println("    PathAction.getConfig 'skip' for " + name + " was NOT true");
					}
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
