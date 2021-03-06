package org.minnal.core.serializer;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.minnal.utils.serializer.DefaultJsonSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class DefaultJsonSerializerTest {
		
	private DefaultJsonSerializer serializer;
	
	private DummyModel model;
	
	@BeforeMethod
	public void setup() {
		 serializer = new DefaultJsonSerializer();
		 model = new DummyModel("name","value");
	}
	
	@Test
	public void shouldSerializeModel(){
		String content = serializer.serialize(model);
		assertEquals("{\"name\":\"name\",\"value\":\"value\",\"composites\":null,\"association\":null}", content); 
	}
	
	@Test
	public void shouldDeserializeModel(){
		String content = serializer.serialize(model);
		DummyModel dummyModel= serializer.deserialize(content, DummyModel.class);
		assertEquals(dummyModel.name,model.name); 
	}
	
	public DummyModel createNestedDummyModel(){
		 DummyModel assosiationModel = new DummyModel("name","value");
		 DummyModel nestedModel = new DummyModel("name","value");
		 Set<DummyModel> nestedModelSet = new HashSet<DummyModel>();
		 nestedModelSet.add(nestedModel);
		 model.setAssociation(assosiationModel);
		 model.setComposites(nestedModelSet);
		 return model;
	}
	
	@Test
	public void shouldRegisterMultipleModules() {
		ObjectMapper mapper = spy(new ObjectMapper());
		serializer = new DefaultJsonSerializer(mapper) {
			@Override
			protected void registerModules(ObjectMapper mapper) {
				mapper.registerModule(new SimpleModule());
				mapper.registerModule(new SimpleModule());
				mapper.registerModule(new SimpleModule());
			}
		};
		verify(mapper, times(3)).registerModule(any(Module.class));
	}

	
	public static class DummyModel{
		
		private String name;
		
		private String value;
		
		private Set<DummyModel> composites;
		
		private DummyModel association;
		
		public DummyModel(){
		}
		
		public DummyModel(String name, String value){
			this.name = name;
			this.value = value;
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * @return the composites
		 */
		public Set<DummyModel> getComposites() {
			return composites;
		}

		/**
		 * @param composites the composites to set
		 */
		public void setComposites(Set<DummyModel> composites) {
			this.composites = composites;
		}

		/**
		 * @return the association
		 */
		public DummyModel getAssociation() {
			return association;
		}

		/**
		 * @param association the association to set
		 */
		public void setAssociation(DummyModel association) {
			this.association = association;
		}
	}
	
}
