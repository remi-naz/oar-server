package dev.ridill.oar_server.common.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void generatesRequestIdAndEchoesItOnResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String requestId = response.getHeader(RequestContext.REQUEST_ID_HEADER);
        assertThat(requestId).isNotBlank();
        assertThat(MDC.get(RequestContext.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void reusesIncomingRequestIdHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader(RequestContext.REQUEST_ID_HEADER, "incoming-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestContext.REQUEST_ID_HEADER)).isEqualTo("incoming-id");
    }
}
