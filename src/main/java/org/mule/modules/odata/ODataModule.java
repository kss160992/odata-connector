/**
* Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
**/

/**
 * This file was automatically generated by the Mule Development Kit
 */
package org.mule.modules.odata;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.odata.factory.ODataConsumerFactory;
import org.mule.modules.odata.factory.ODataConsumerFactoryImpl;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.Guid;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OQueryRequest;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatType;

/**
 * Mule Module that provides the basic operations to consume
 * an OData service.
 *  
 * @author mariano.gonzalez@mulesoft.com
 */
@Module(name="odata", schemaVersion="1.0-SNAPSHOT")
public class ODataModule {
	
	private static final Logger logger = Logger.getLogger(ODataModule.class);
	private static final PropertyUtilsBean propertyUtils = new PropertyUtilsBean();
	
	/***
	 * The OData service root uri 
	 */
	@Configurable
	private String baseServiceUri;
	
	/**
	 * If the odata service requires authentication, set your username
	 * in this attribute. Notice that setting this property but leaving the password
	 * blank will result in an exception while initializing this module
	 */
	@Configurable
	@Optional
	private String username;
	
	/**
	 * If the odata service requires authentication, set your password
	 * in this attribute. Notice that setting this property but leaving the username
	 * blank will result in an exception while initializing this module
	 */
	@Configurable
	@Optional
	private String password;
	
	/**
	 * An instance of {@link org.mule.modules.odata.factory.ODataConsumerFactory}
	 * to intanciate the {@link org.odata4j.consumer.ODataConsumer}. Normally you don't
	 * need to set this unless you require some custom initialization of the consumer
	 * or if you are doing test cases.
	 * 
	 * If this property is not specified, then an instance of
	 * {@link org.mule.modules.odata.factory.ODataConsumerFactoryImpl.ODataConsumerFactoryImpl} is used 
	 */
	@Configurable
	@Optional
	private ODataConsumerFactory consumerFactory;
	
	/**
	 * The consumer to use
	 */
	private ODataConsumer consumer;
	
	/**
	 * The namig policy to be used when mapping pojo's attributes to OData entities.
	 * Depending on the OData service you're consuming, you might find that attributes usually follows a
	 * lower camel case format (e.g.: theAttribute) or an upper camel case format (e.g.: TheAttribute).
	 * 
	 * The naming format assumes that your pojo's properties follow the lower camel case
	 * format (just as the java coding standard dictates) and translates to the format that the OData service
	 * is expecting.
	 * 
	 * Valid values are: LOWER_CAMEL_CASE and UPPER_CAMEL_CASE.
	 */
	@Configurable
	@Optional
	@Default("LOWER_CAMEL_CASE")
	private PropertyNamingFormat namingFormat = PropertyNamingFormat.LOWER_CAMEL_CASE;
	
	/**
	 * The format of the payload to be used during communication.
	 * Valid values are JSON and ATOM
	 */
	@Configurable
	@Optional
	@Default("JSON")
	private FormatType formatType = FormatType.JSON;
	
	/**
	 * This method initializes the module by creating the consumer and the factory (if needed)
	 */
	@Start
	public void init() {
		if (this.consumerFactory == null) {
			this.consumerFactory = new ODataConsumerFactoryImpl();
		}
		
		this.consumer = this.consumerFactory.newConsumer(this.baseServiceUri, this.formatType, this.username, this.password);
	}

    /**
     * Reads entities from an specified set and returns it as a list of pojos
     *
     * {@sample.xml ../../../doc/OData-connector.xml.sample odata:get-as-pojos}
     *
     * @param returnClass the canonical class name for the pojo instances to be returned
     * @param entitySetName the name of the set to be read
     * @param filter an OData filtering expression. If not provided, no filtering occurs (see http://www.odata.org/developers/protocols/uri-conventions#FilterSystemQueryOption)
     * @param orderBy the ordering expression. If not provided, no ordering occurs (see http://www.odata.org/developers/protocols/uri-conventions#OrderBySystemQueryOption(
     * @param skip number of items to skip, usefull for pagination. If not provided, no records are skept (see http://www.odata.org/developers/protocols/uri-conventions#SkipSystemQueryOption)
     * @param top number of items to return (see http://www.odata.org/developers/protocols/uri-conventions#TopSystemQueryOption)
     * @param select the selection clauses. If not specified, all fields are returned (see http://www.odata.org/developers/protocols/uri-conventions#SelectSystemQueryOption)
     * @return a list of objects of class "returnClass" representing the obtained entities
     */
    @Processor
    @SuppressWarnings("unchecked")
    public List<Object> getAsPojos(
    						String returnClass,
    						String entitySetName,
    						@Optional String filter,
    						@Optional String orderBy,
    						@Optional String expand,
    						@Optional Integer skip,
    						@Optional Integer top,
    						@Optional String select) {
    	
    	Class<?> clazz = null;
    	
    	try {
    		clazz = Class.forName(returnClass);
    	} catch (ClassNotFoundException e) {
    		throw new IllegalArgumentException(String.format("return class %s not found in classpath", returnClass), e);
    	}
    	
    	
    	OQueryRequest<?> request =  this.consumer.getEntities(clazz, entitySetName)
										.filter(filter)
										.orderBy(orderBy)
										.expand(expand)
										.select(select);
    	
    	if (skip != null) {
    		request.skip(skip);
    	}
    	
    	if (top != null) {
    		request.top(top);
    	}
    	
    	return (List<Object>) request.execute().toList();
    }
    
    /**
     * Inserts an entity from an input pojo
     * 
     * {@sample.xml ../../../doc/OData-connector.xml.sample odata:create-from-pojo}
     * 
     * @param pojo an object representing the entity
     * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
     * @return an instance of {@link org.odata4j.core.OEntity} representing the entity just created on the OData set
     */
    @Processor
    public OEntity createFromPojo(@Optional @Default("#[payload:]") Object pojo, @Optional String entitySetName) {
    	OCreateRequest<OEntity> entity = this.consumer.createEntity(this.getEntitySetName(pojo, entitySetName));
    	Collection<OProperty<?>> properties = this.populateODataProperties(pojo);
    	
		if (properties != null) {
			entity.properties(properties);
		}
		
		return entity.execute();
    }
    
    /**
     * Inserts entities from an input list of pojos
     * 
     * {@sample.xml ../../../doc/OData-connector.xml.sample odata:create-from-pojos-list}
     * 
     * @param pojos a list of pojos representing the entities you want to create on the OData service
     * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
     * @return a list with instances of {@link org.odata4j.core.OEntity} representing the entities just created on the OData set
     */
    @Processor
    public List<OEntity> createFromPojosList(@Optional @Default("#[payload:}") List<Object> pojos, @Optional String entitySetName) {
    	
    	if (pojos == null || pojos.isEmpty()) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("empty pojos list received, exiting without doing anything");
    		}
    		
    		return Collections.emptyList();
    	}
    	
    	entitySetName = this.getEntitySetName(pojos.get(0), entitySetName);
    	
    	List<OEntity> entities = new ArrayList<OEntity>(pojos.size());
    	
    	for (Object pojo : pojos) {
    		entities.add(this.createFromPojo(pojo, entitySetName));
    	}
    	return entities;
    }
    
    /**
     * Updates an entity represented by a pojo on the OData service
     * 
     * {@sample.xml ../../../doc/OData-connector.xml.sample odata:update-from-pojo}
     * 
     * @param pojo an object representing the entity
     * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
     * @param keyAttribute the name of the pojo's attribute that holds the entity's key. The attribute cannot hold a null value
     */
    @Processor
    public void updateFromPojo(@Optional @Default("#[payload:]") Object pojo, @Optional String entitySetName, String keyAttribute) {
    	OModifyRequest<OEntity> request = this.consumer.mergeEntity(this.getEntitySetName(pojo, entitySetName), this.extractValue(pojo, keyAttribute));
    	Collection<OProperty<?>> properties = this.populateODataProperties(pojo);

		if (properties != null) {
			request.properties(properties);
		}
		
		request.execute();
    }
    
    /**
     * Deletes an entity represented by a pojo on the OData service
     * 
     * {@sample.xml ../../../doc/OData-connector.xml.sample odata:delete-from-pojo}
     * 
     * @param pojo an object representing the entity
     * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
     * @param keyAttribute the name of the pojo's attribute that holds the entity's key. The attribute cannot hold a null value
     */
    @Processor
    public void deleteFromPojo(@Optional @Default("#[payload:]") Object pojo, @Optional String entitySetName, String keyAttribute) {
    	this.consumer.deleteEntity(this.getEntitySetName(pojo, entitySetName), this.extractValue(pojo, keyAttribute));
    }

	private Object extractValue(Object pojo, String keyAttribute) {
		assert pojo != null : "pojo cannot be null";
		assert !StringUtils.isBlank(keyAttribute) : "ket attribute cannot be null";
		
		Object keyValue = null;
    	
		try {
    		
    		keyValue = propertyUtils.getProperty(pojo, this.namingFormat.toJava(keyAttribute));
    		
    		if (keyValue == null) {
    			throw new IllegalStateException(String.format("the key attribute %s on pojo of class %s cannot be null", keyAttribute, pojo.getClass().getCanonicalName()));
    		}
    		
    		return keyValue;
    		
    	} catch (IllegalAccessException e) {
    		this.handleReadPropertyException(pojo, keyAttribute, e);
    	} catch (NoSuchMethodException e) {
    		this.handleReadPropertyException(pojo, keyAttribute, e);
    	} catch (InvocationTargetException e) {
    		this.handleReadPropertyException(pojo, keyAttribute, e);
    	}
		
		return keyValue;
	}
    
    private void handleReadPropertyException(Object pojo, String propertyName, Exception e) {
    	throw new RuntimeException(String.format("Could not read property %s on pojo of class %s", propertyName, pojo.getClass().getCanonicalName()), e);
    }
    
    private String getEntitySetName(Object pojo, String entitySetName) {
    	if (pojo == null) {
    		throw new IllegalArgumentException("cannot use a null pojo");
    	}
    	
    	return StringUtils.isBlank(entitySetName) ? pojo.getClass().getSimpleName() + "Set" : entitySetName; 
    }
     
    private <T> Collection<OProperty<?>> populateODataProperties(T object) {
		Map<String, PropertyDescriptor> properties = this.describe(object.getClass());
		
		if (properties.isEmpty()) {
			return null;
		}
		
		Collection<OProperty<?>> result = new ArrayList<OProperty<?>>(properties.size());
		
		try {
			for (PropertyDescriptor prop : properties.values()) {
				Object value = prop.getReadMethod().invoke(object, (Object[]) null);
				
				if (value != null) {
					
					String key = this.namingFormat.toOData(prop.getName());
					
					if (value instanceof Guid) {
						result.add(OProperties.guid(key, (Guid) value));
					} else if (this.isSimpleType(value)) {
						result.add(OProperties.simple(key, value));
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
    
	private <T> Map<String, PropertyDescriptor> describe(Class<T> clazz) {
		BeanInfo info = null;
		try {
			info = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new RuntimeException();
		}
		
		Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor property : info.getPropertyDescriptors()) {
			if (property.getReadMethod() != null && property.getWriteMethod() != null) {
				map.put(property.getName(), property);
			}
		}
		
		return map;
	}
	
	private boolean isSimpleType(Object value) {
		return this.isSimpleType(value.getClass());
	}
	
	private boolean isSimpleType(Class<?> clazz) {
		return EdmSimpleType.forJavaType(clazz) != null;
	}
    
	public String getBaseServiceUri() {
		return baseServiceUri;
	}

	public void setBaseServiceUri(String baseServiceUri) {
		this.baseServiceUri = baseServiceUri;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ODataConsumerFactory getConsumerFactory() {
		return consumerFactory;
	}

	public void setConsumerFactory(ODataConsumerFactory consumerFactory) {
		this.consumerFactory = consumerFactory;
	}

	public ODataConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(ODataConsumer consumer) {
		this.consumer = consumer;
	}

	public PropertyNamingFormat getNamingFormat() {
		return namingFormat;
	}

	public void setNamingFormat(PropertyNamingFormat namingFormat) {
		this.namingFormat = namingFormat;
	}

	public FormatType getFormatType() {
		return formatType;
	}

	public void setFormatType(FormatType formatType) {
		this.formatType = formatType;
	}
	
}
