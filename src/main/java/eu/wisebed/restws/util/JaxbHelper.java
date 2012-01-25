package eu.wisebed.restws.util;

import com.google.common.base.Throwables;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.StringWriter;

@SuppressWarnings("deprecation")
public class JaxbHelper {

	private static ObjectMapper mapper = new ObjectMapper();

	private static AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();

	static {
		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
		// make serializer use JAXB annotations (only)
		mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
	}

	public static String convertToXML(Object o) {
		StringWriter writer = new StringWriter();
		javax.xml.bind.JAXB.marshal(o, writer);
		return writer.toString();
	}

	public static String convertToJSON(Object o) {
		StringWriter writer = new StringWriter();
		try {
			mapper.writeValue(writer, o);
		} catch (IOException e) {
		}
		return writer.toString();
	}

	public static <T> T fromJSON(String json, Class<T> type) throws Exception {
		try {
			return mapper.readValue(json, type);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

}
