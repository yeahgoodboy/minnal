/**
 * 
 */
package org.minnal.instrument.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.minnal.instrument.NamingStrategy;
import org.minnal.instrument.UnderscoreNamingStrategy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author ganeshs
 * 
 */
public class ResponseTransformationFilter implements ContainerResponseFilter {

	public static final String EXCLUDE = "exclude";

	public static final String INCLUDE = "include";

	private NamingStrategy namingStrategy = new UnderscoreNamingStrategy();
	
	private List<String> propertiesToExclude;

	/**
	 * @param propertiesToExclude
	 */
	public ResponseTransformationFilter(List<String> propertiesToExclude) {
		this.propertiesToExclude = propertiesToExclude;
	}

	private Set<String> splitParams(String paramValue) {
		final Iterable<String> values = Splitter.on(",").trimResults().omitEmptyStrings().split(Strings.nullToEmpty(paramValue));
		return Sets.newHashSet(Iterables.transform(values, new Function<String, String>() {
			@Override
			public String apply(String input) {
				// TODO use naming strategy instead
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, input);
			}
		}));
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
		Set<String> excludes = splitParams(queryParams.getFirst(EXCLUDE));
		Set<String> includes = splitParams(queryParams.getFirst(INCLUDE));
		excludes.addAll(propertiesToExclude);
		ExclusionObjectModifier modifier = new ExclusionObjectModifier(excludes, includes);
		ObjectWriterInjector.set(modifier);
	}

	/**
	 * @author ganeshs
	 *
	 */
	public static class ExclusionObjectModifier extends ObjectWriterModifier {
		
		private Set<String> excludes;
		
		private Set<String> includes;
		
		/**
		 * @param excludes
		 * @param includes
		 */
		public ExclusionObjectModifier(Set<String> excludes, Set<String> includes) {
			this.excludes = excludes;
			this.includes = includes;
		}

		@Override
		public ObjectWriter modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, Object> responseHeaders, Object valueToWrite,
				ObjectWriter w, JsonGenerator g) throws IOException {
			SimpleBeanPropertyFilter filter = null;
			if (includes != null && !includes.isEmpty()) {
				filter = new SimpleBeanPropertyFilter.FilterExceptFilter(includes);
			} else if (excludes != null && !excludes.isEmpty()) {
				filter = SimpleBeanPropertyFilter.serializeAllExcept(excludes);
			} else {
				filter = SimpleBeanPropertyFilter.serializeAllExcept(new HashSet<String>());
			}
			FilterProvider provider = new SimpleFilterProvider().addFilter("property_filter", filter);
			return w.with(provider);
		}
	}
}