package eu.wisebed.restws.resources.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeUrnList {
	public List<String> nodeUrns;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeUrnList [nodeUrns=");
		builder.append(nodeUrns);
		builder.append("]");
		return builder.toString();
	}
}
