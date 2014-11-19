import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;


public class NumberNameToNumber {

	private static String appid = "XKXW7Q-RUTH97KAHA";
	public static String convert(String name) {
		String ans = null;
		if (name.matches("[0-9]+"))
			return name;
		WAEngine engine = new WAEngine();
		engine.setAppID(appid);
		engine.addFormat("plaintext");
		WAQuery query = engine.createQuery();
		query.setInput(name);
		try {
			WAQueryResult queryResult = engine.performQuery(query);
			if (queryResult.isError()) {
				System.out.println("Query error");
				System.out.println(" error code: " + queryResult.getErrorCode());
				System.out.println(" error message: " + queryResult.getErrorMessage());
			} else if (!queryResult.isSuccess()) {
				System.out.println("Query was not understood; no results available.");
			} else {
				System.out.println("Successful query. Pods follow:\n");
				for (WAPod pod : queryResult.getPods()) {
					if (!pod.isError() && pod.getTitle().equals("Input")) {
						WASubpod subpod = pod.getSubpods()[0];
						for (Object element : subpod.getContents()) {
							if (element instanceof WAPlainText) {
								ans = ((WAPlainText) element).getText();
								System.out.println(ans);
							}
						}
						break;
					}
				}
			}
		}
		catch (WAException e) {
			e.printStackTrace();
		}
		return ans;
	}
}
