import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public ArrayList<HTTPSampler> httpSamplers = new ArrayList<HTTPSampler>();
	File openedFile;

	public Main(File file) {
		loadHTTPSamplers(file);
		System.out.printf("File: %s\n",file.getName());
		List<String> oldMatchedValues = null;
		String oldName = "";
		int identicalNum = 0;
		int times = 0;
		String prevSamplerName = "";
		int loopCount = 0;
		for (HTTPSampler sampler : httpSamplers) {
			loopCount++;
			if (loopCount == httpSamplers.size()-1) { // Если мы в конце цикла, печатаем всё без проверки
				if (identicalNum > 0) {
					printUncorrelatedMany(oldName, identicalNum, prevSamplerName, oldMatchedValues);
				} else if (oldMatchedValues != null) { // Надо обязательно проверить на null потому что, если у нас ничего в файле не найдено, то список не инициализируется
					printUncorrelated(oldName, oldMatchedValues);
				}
				break;
			}
			List<String> matchedValues = sampler.check();
			if (matchedValues == null || matchedValues.isEmpty()) continue;
			if (++times == 1) { // При первых найденных не скоррелированных значениях у нас ещё нет информации о предыдущих значениях, поэтому нам не надо пока проверять, отличаются ли эти значения от предыдущих
				oldName = sampler.name;
				oldMatchedValues = matchedValues;
				continue;
			}

			if (matchedValues.equals(oldMatchedValues)) { // Печатаем не скоррелированные значения только если у нас список значений не отличиется от предыдущих итераций
				identicalNum++;
			} else {
				if (identicalNum > 0) {
					printUncorrelatedMany(oldName, identicalNum, prevSamplerName, oldMatchedValues);
				} else {
					printUncorrelated(oldName, oldMatchedValues);
				}
				identicalNum = 0;
				oldName = sampler.name;
			}
			prevSamplerName = sampler.name;
			oldMatchedValues = matchedValues;
		}
	}

	void printUncorrelated(String name, List<String> printValues) {
		System.out.printf("\tSampler: %s\n", name);
		int printNum = 0;
		System.out.print("\t\t");
		for (String uncorrelated : printValues) {
			System.out.print(uncorrelated);
			if (++printNum < printValues.size()) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}

	void printUncorrelatedMany(String oldName, int repeatedNum, String name, List<String> printValues) {
		System.out.printf("\tSamplers: %s .. ещё %d запросов .. %s\n", oldName, repeatedNum, name);
		System.out.print("\t\t");
		int printNum = 0;
		for (String uncorrelated : printValues) {
			System.out.print(uncorrelated);
			if (++printNum < printValues.size()) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}

	public void loadHTTPSamplers(File fXmlFile) {
		httpSamplers.clear();
		openedFile = fXmlFile;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(openedFile);

			NodeList httpSamplersNodes = doc.getElementsByTagName("HTTPSamplerProxy");
			Node sampler;
			for (int i = 0; true; i++) {
				sampler = httpSamplersNodes.item(i);
				if (sampler == null) {
					break;
				}

				String samplerName = sampler.getAttributes().getNamedItem("testname").getNodeValue();
				Node stringProp = findNodeWithAttribure(sampler, "stringProp", "name", "Argument.value");

				String textContent = null;
				if (stringProp != null) {
					textContent = stringProp.getTextContent();
				}

				HTTPSampler HttpSampler = new HTTPSampler(samplerName, i, textContent);
				httpSamplers.add(HttpSampler);
			}

			HTTPSampler[] samplersArray = new HTTPSampler[httpSamplers.size()];
			httpSamplers.toArray(samplersArray);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	public Node findNodeWithAttribure(Node rootNode, String tag, String attr, String attrValue) {

		Element rootElement = (Element) rootNode;
		NodeList stringProps = rootElement.getElementsByTagName(tag);

		Node node = stringProps.item(0);

		int i = 0;
		while (true) {
			if (node == null) break;
			String nameAttr = node.getAttributes().getNamedItem(attr).getNodeValue();
			if (nameAttr.equals(attrValue)) {
				break;
			}
			node = stringProps.item(i);
			i++;
		}
		return node;
	}

	public static void main(String[] args) {
		for (String arg : args) {
			new Main(new File(arg));
		}
	}
}
