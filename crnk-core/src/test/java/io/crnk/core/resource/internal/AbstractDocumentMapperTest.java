package io.crnk.core.resource.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.filter.FilterBehaviorDirectory;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractDocumentMapperTest {

	protected DocumentMapper mapper;

	protected ResourceRegistry resourceRegistry;

	protected ObjectMapper objectMapper;

	protected FilterBehaviorDirectory filterBehaviorDirectory;
	private ModuleRegistry moduleRegistry;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		// TODO

		//CrnkBoot boot = new CrnkBoot();
		//boot.setServiceDiscovery(new ReflectionsServiceDiscovery(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE));
		//boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		//boot.boot();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build());


		ConstantServiceUrlProvider serviceUrlProvider = new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL);

		ResourceInformationBuilder resourceInformationBuilder =
				new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer(), new JacksonResourceFieldInformationProvider(objectMapper));
		moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder registryBuilder =
				new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry,
				serviceUrlProvider);


		moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(serviceUrlProvider);
		moduleRegistry.init(objectMapper);

		filterBehaviorDirectory = moduleRegistry.getContext().getFilterBehaviorProvider();

		mapper = new DocumentMapper(resourceRegistry, objectMapper, getPropertiesProvider(), filterBehaviorDirectory);
	}

	protected PropertiesProvider getPropertiesProvider() {
		return null;
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryAdapter createAdapter(Class resourceClass) {
		return new QueryParamsAdapter(resourceRegistry.getEntry(resourceClass).getResourceInformation(), new QueryParams(), moduleRegistry);
	}

	protected QueryAdapter toAdapter(QuerySpec querySpec) {
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

	protected JsonApiResponse toResponse(Object entity) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(entity);
		return response;
	}

}
