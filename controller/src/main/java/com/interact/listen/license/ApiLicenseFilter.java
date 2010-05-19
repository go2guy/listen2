package com.interact.listen.license;

import com.interact.listen.ApiResourceLocatorFilter;
import com.interact.listen.resource.Resource;

import java.io.IOException;

import javax.servlet.*;

public class ApiLicenseFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        final String key = ApiResourceLocatorFilter.RESOURCE_CLASS_KEY;
        Class<? extends Resource> resourceClass = (Class<? extends Resource>)request.getAttribute(key);

        for(ListenFeature feature : ListenFeature.getFeaturesWithResourceClass(resourceClass))
        {
            if(!License.isLicensed(feature))
            {
                throw new ServletException(new NotLicensedException(feature));
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        // no implementation
    }

    @Override
    public void destroy()
    {
        // no implementation
    }
}
