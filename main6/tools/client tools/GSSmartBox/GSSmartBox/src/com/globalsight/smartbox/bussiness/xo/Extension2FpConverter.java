package com.globalsight.smartbox.bussiness.xo;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * @author fuliang
 * 
 */
public class Extension2FpConverter extends AbstractCollectionConverter {

	public Extension2FpConverter(Mapper mapper) {
		super(mapper);
	}

	@Override
	public boolean canConvert(Class type) {
		// Used by java.awt.Font in JDK 6
		return type.equals(HashMap.class) || type.equals(Hashtable.class)
				|| type.getName().equals("java.util.LinkedHashMap") || type.getName().equals("sun.font.AttributeMap");
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		Map map = (Map) source;
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry entry = (Entry) iterator.next();
			ExtendedHierarchicalStreamWriterHelper.startNode(writer, "property", Entry.class);

			writer.addAttribute("fpName", entry.getKey().toString());
			writer.addAttribute("extension", entry.getValue().toString());
			writer.endNode();
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Map map = (Map) createCollection(context.getRequiredType());
		populateMap(reader, context, map);
		return map;
	}

	protected void populateMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map<String, String> map) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			// GeneralPreProcess line 164
			String value = reader.getAttribute("fpName");
			String keys = reader.getAttribute("extension");
            for (String key : keys.split(","))
            {
                key = key.trim().toLowerCase();
                map.put(key, value);
            }
			reader.moveUp();
		}
	}
}
