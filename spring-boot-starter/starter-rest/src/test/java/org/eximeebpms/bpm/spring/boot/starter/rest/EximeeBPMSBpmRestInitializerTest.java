package org.eximeebpms.bpm.spring.boot.starter.rest;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.eximeebpms.bpm.engine.rest.filter.CacheControlFilter;
import org.eximeebpms.bpm.engine.rest.filter.EmptyBodyFilter;
import org.eximeebpms.bpm.spring.boot.starter.property.EximeeBPMSBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.property.RestApiProperty;
import org.eximeebpms.bpm.spring.boot.starter.property.FetchAndLockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EximeeBPMSBpmRestInitializerTest {

  @Mock
  private JerseyApplicationPath applicationPath;
  @Mock
  private EximeeBPMSBpmProperties properties;
  @Mock
  private RestApiProperty restApiProperty;
  @Mock
  private FetchAndLockProperties fetchAndLockProperties;
  @Mock
  private ServletContext servletContext;
  @Mock
  private FilterRegistration.Dynamic filterRegistration;
  private EximeeBPMSBpmRestInitializer uut;

  @BeforeEach
  void setUp() {
    when(applicationPath.getUrlMapping()).thenReturn("/rest/*");
    when(properties.getRestApi()).thenReturn(restApiProperty);
    when(restApiProperty.getFetchAndLock()).thenReturn(fetchAndLockProperties);
    when(fetchAndLockProperties.getInitParams()).thenReturn(Map.of("foo", "bar"));

    uut = new EximeeBPMSBpmRestInitializer(applicationPath, properties);
  }

  @Test
  void shouldRegistersFilters() {
    // given
    when(servletContext.getFilterRegistration(anyString())).thenReturn(null);
    when(servletContext.addFilter("EmptyBodyFilter", EmptyBodyFilter.class)).thenReturn(filterRegistration);
    when(servletContext.addFilter("CacheControlFilter", CacheControlFilter.class)).thenReturn(filterRegistration);

    // when
    uut.onStartup(servletContext);

    // then
    verify(servletContext).setInitParameter("foo", "bar");
    verify(servletContext).addFilter("EmptyBodyFilter", EmptyBodyFilter.class);
    verify(servletContext).addFilter("CacheControlFilter", CacheControlFilter.class);
    verify(filterRegistration, times(2)).addMappingForUrlPatterns(any(), eq(true), eq("/rest/*"));
  }

  @Test
  void shouldFilterAlreadyRegisteredAndDoesNotRegisterAgain() {
    // given
    final FilterRegistration existingRegistration = mock(FilterRegistration.class);
    when(servletContext.getFilterRegistration(anyString())).thenReturn(existingRegistration);

    // when
    uut.onStartup(servletContext);

    // then
    verify(servletContext, never()).addFilter(anyString(), any(Class.class));
  }

}
