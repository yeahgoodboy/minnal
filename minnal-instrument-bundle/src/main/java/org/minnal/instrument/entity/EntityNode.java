/**
 * 
 */
package org.minnal.instrument.entity;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.atteo.evo.inflector.English;
import org.minnal.core.util.Node;
import org.minnal.instrument.entity.EntityNode.EntityNodePath;
import org.minnal.instrument.entity.metadata.CollectionMetaData;
import org.minnal.instrument.entity.metadata.EntityMetaData;
import org.minnal.instrument.entity.metadata.EntityMetaDataProvider;

import com.google.common.base.CaseFormat;

/**
 * @author ganeshs
 *
 */
public class EntityNode extends Node<EntityNode, EntityNodePath> {

	private String name;
	
	private EntityMetaData entityMetaData;
	
	public EntityNode(Class<?> entityClass) {
		this(entityClass, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, English.plural(entityClass.getSimpleName())));
	}
	
	public EntityNode(Class<?> entityClass, String name) {
		this.name = name;
		this.entityMetaData = EntityMetaDataProvider.instance().getEntityMetaData(entityClass);
		populateChildren();
	}

	private void populateChildren() {
		for (CollectionMetaData collection : entityMetaData.getCollections()) {
			if (! collection.isEntity()) {
				continue;
			}
			addChild(new EntityNode(collection.getElementType(), collection.getName()));
		}
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the entityMetaData
	 */
	public EntityMetaData getEntityMetaData() {
		return entityMetaData;
	}

	@Override
	protected EntityNode getThis() {
		return this;
	}
	
	@Override
	protected EntityNodePath createNodePath(List<EntityNode> path) {
		return new EntityNodePath(path);
	}
	
	public class EntityNodePath extends Node<EntityNode, EntityNodePath>.NodePath {
		
		private String bulkPath;
		
		private String singlePath;

		public EntityNodePath(List<EntityNode> path) {
			super(path);
			init(path);
		}
		
		private void init(List<EntityNode> path) {
			StringWriter writer = new StringWriter();
			Iterator<EntityNode> iterator = iterator();
			while (iterator.hasNext()) {
				EntityNode node = iterator.next();
				String name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getName());
				writer.append("/").append(name);
				if (iterator.hasNext()) {
					writer.append("/{" + name + "_id}");
				}
			}
			bulkPath = writer.toString();
			singlePath = bulkPath + "/{id}";
		}
		
		public String getBulkPath() {
			return bulkPath;
		}
		
		public String getSinglePath() {
			return singlePath;
		}
		
		public String getName() {
			return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, get(size() - 1).getName());
		}

	}
}