import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPSampler {
	public String bodyData;
	public String name;
	public int id;

	public HTTPSampler(String name, int id, String bodyData) {
		this.name = name;
		this.bodyData = bodyData;
		this.id = id;
	}

	String filterUIDandVars() {
		Matcher uidMatcher = Pattern.compile("(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}|\\$\\{[^}]+)").matcher(bodyData);
		return uidMatcher.replaceAll("");
	}
	public List<String> check() {
		if (bodyData == null) return null;
		String filtered = filterUIDandVars();
		Matcher numberMatcher = Pattern.compile("\\d{4,}").matcher(filtered);
		Matcher dateMatcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(filtered);
		Matcher timeMatcher = Pattern.compile("\\d{2}:\\d{2}:\\d{2}").matcher(filtered);
		List<String> matchedValues = new LinkedList();
		while (numberMatcher.find()) {
			matchedValues.add(numberMatcher.group(0));
		}
		while (dateMatcher.find()) {
			matchedValues.add(dateMatcher.group(0));
		}
		while (timeMatcher.find()) {
			String matchedStr = timeMatcher.group(0);
			if (!matchedStr.equals("00:00:00") && !matchedStr.equals("23:59:59")) {
				matchedValues.add(timeMatcher.group(0));
			}
		}
		return matchedValues;
	}
}
